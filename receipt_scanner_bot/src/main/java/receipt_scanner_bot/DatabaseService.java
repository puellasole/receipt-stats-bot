package receipt_scanner_bot;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class DatabaseService {
    
    private final PurchaseDetailRepository purchaseDetailRepository;
    private final ProductStatRepository productStatRepository;
    
    public DatabaseService(PurchaseDetailRepository purchaseDetailRepository,
                          ProductStatRepository productStatRepository) {
        this.purchaseDetailRepository = purchaseDetailRepository;
        this.productStatRepository = productStatRepository;
    }
    
    @Transactional
    public void saveProducts(List<Product> products) {
    	/*
        if (products == null || products.isEmpty()) {
            return;
        }
        List<PurchaseDetailEntity> purchaseDetails = new ArrayList<>();
        List<ProductStatEntity> productStats = new ArrayList<>();
        for(Product p : products) {
        	purchaseDetails.add(convertToPurchaseDetail(p));
        	productStats.add(convertToProductStat(p));
        }
        //
        // Сохраняем в основную таблицу
        List<PurchaseDetailEntity> purchaseDetails = products.stream()
            .map(this::convertToPurchaseDetail)
            .collect(Collectors.toList());
        
        // Сохраняем в таблицу статистики
        List<ProductStatEntity> productStats = products.stream()
            .map(this::convertToProductStat)
            .collect(Collectors.toList());
        //
        purchaseDetailRepository.saveAll(purchaseDetails);
        productStatRepository.saveAll(productStats);
        */
        try {
            List<PurchaseDetailEntity> purchaseDetails = new ArrayList<>();
            List<ProductStatEntity> productStats = new ArrayList<>();
            
            for (Product product : products) {
                purchaseDetails.add(convertToPurchaseDetail(product));
                productStats.add(convertToProductStat(product));
            }
            
            // Все в одной транзакции - либо сохранится все, либо ничего
            purchaseDetailRepository.saveAll(purchaseDetails);
            productStatRepository.saveAll(productStats);
            
        } catch (Exception e) {
            // Транзакция откатится автоматически благодаря @Transactional
            throw new RuntimeException("Failed to save products", e);
        }
    }
    
    //prodname, quantity sum, amount sum (multiple products)
    //по идее должна возвращать мапу (строка, статка: кол-во, цена)
    //а почему не массив?
    @Transactional(readOnly = true)
    public String getStatsForAllProducts() {
        List<PurchaseDetailEntity> allPurchases = purchaseDetailRepository.findAllByOrderByPurchaseDateAscProductNameAsc();
        
        if (allPurchases.isEmpty()) {
        	//тут должно возвращать исключение (или лог ошибки?)
            return "📊 База данных пуста. Добавьте сначала несколько чеков.";
        }
        
        StringBuilder stats = new StringBuilder();
        stats.append("📊 ОБЩАЯ СТАТИСТИКА ПО ВСЕМ ПРОДУКТАМ:\n\n");
        
        //map<string, abstractMap.simpleEntry<Integer, BigDecimal>
        Map<String, AbstractMap.SimpleEntry<Integer, BigDecimal>> productStats = allPurchases.stream()
                .collect(Collectors.groupingBy(
                    PurchaseDetailEntity::getProductName,
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            int totalQuantity = list.stream().mapToInt(PurchaseDetailEntity::getQuantity).sum();
                            BigDecimal totalAmount = list.stream()
                                .map(PurchaseDetailEntity::getPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                            return new AbstractMap.SimpleEntry<>(totalQuantity, totalAmount);
                        }
                    )
                ));
        
        //from this point on data representation 
        int rank = 1;
        for (var entry : productStats.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().getValue().compareTo(e1.getValue().getValue()))
                .collect(Collectors.toList())) {
            
            String productName = entry.getKey();
            int totalQuantity = entry.getValue().getKey();
            BigDecimal totalSpent = entry.getValue().getValue();
            
            stats.append(String.format("%d.  →  %.2f ₽    (%d шт.)\n", 
            	    rank, totalSpent.doubleValue(), totalQuantity));
            stats.append(String.format("   %s\n\n", productName));
            rank++;
        }
        
        return stats.toString();
    }
    
    //new exception if the stats is empty (or logs?)
    //prodname, unit price, trend 
    //didnt you already get all the stats wrapped in entity classes (you're getting a list from db
    //except for the trend (can the entity class have field that aren't annotated with columns) 
    //trend can be separated and passed onto gui methods /classes
    //есть еще разбор, среднее и диапазон (в виде мин и макс цены)
    @Transactional(readOnly = true)
    public String getStatsForProduct(String productName) {
        List<ProductStatEntity> stats = productStatRepository.findByProductNameOrderByStatDateAsc(productName);
        
        if (stats.isEmpty()) {
            return "❌ Продукт '" + productName + "' не найден в базе данных.\n\n" +
                   "💡 Попробуйте поискать похожие названия или проверьте правильность написания.";
        }
        
        return buildProductStatistics(stats, productName);
    }
    
    private String buildProductStatistics(List<ProductStatEntity> stats, String productName) {
        StringBuilder result = new StringBuilder();
        
        // Основная статистика
        //тут конвертация bigdec в double
        DoubleSummaryStatistics priceStats = stats.stream()
            .mapToDouble(s -> s.getUnitPrice().doubleValue())
            .summaryStatistics();
        
        // Расчет тренда
        String trend = calculateTrend(stats);
        
        result.append(String.format("📊 СТАТИСТИКА: %s\n\n", productName.toUpperCase()));
        
        // Диапазон цен
        result.append("💰 Диапазон цен: ");
        //result.append(String.format("   • Минимальная: %.2f руб.\n", priceStats.getMin()));
        //result.append(String.format("   • Максимальная: %.2f руб.\n", priceStats.getMax()));
        result.append(String.format("%.2f - %.2f руб.\n", priceStats.getMin(), priceStats.getMax()));
        result.append(String.format("   • Средняя: %.2f руб.\n", priceStats.getAverage()));
        result.append(String.format("   • Разброс: %.2f руб.\n\n", priceStats.getMax() - priceStats.getMin()));
        
        // Общая информация
        result.append(" Общий тренд:\n");
        result.append(trend + "\n");
        result.append(String.format("   • Количество записей: %d\n", stats.size()));
        result.append(String.format("   • Период наблюдений: %s - %s\n\n", 
            stats.get(0).getStatDate(), stats.get(stats.size()-1).getStatDate()));
        
        // Последние цены
        result.append("🔄 Последние изменения цен:\n");
        int displayCount = Math.min(stats.size(), 5);
        for (int i = stats.size() - displayCount; i < stats.size(); i++) {
            ProductStatEntity stat = stats.get(i);
            String change = i > 0 ? calculatePriceChange(stats.get(i-1).getUnitPrice(), stat.getUnitPrice()) : "";
            result.append(String.format("   • %s: %.2f руб. %s\n", 
                stat.getStatDate(), stat.getUnitPrice(), change));
        }
        
        return result.toString();
    }
    
    //тут bigdec
    private String calculateTrend(List<ProductStatEntity> stats) {
        if (stats.size() < 2) {
            return "📊 Недостаточно данных для анализа тренда";
        }
        
        BigDecimal firstPrice = stats.get(0).getUnitPrice();
        BigDecimal lastPrice = stats.get(stats.size() - 1).getUnitPrice();
        BigDecimal change = lastPrice.subtract(firstPrice);
        BigDecimal percentChange = change.divide(firstPrice, 4, RoundingMode.HALF_UP)
                                       .multiply(BigDecimal.valueOf(100));
        
        long daysBetween = ChronoUnit.DAYS.between(
            stats.get(0).getStatDate(), 
            stats.get(stats.size() - 1).getStatDate()
        );
        
        //разное отображение для больше/меньше нуля разницы
        if (change.compareTo(BigDecimal.ZERO) > 0) {
            return String.format("📈 Рост +%.2f руб. (+%.1f%%) за %d дней", 
                change.doubleValue(), percentChange.doubleValue(), daysBetween);
        } else if (change.compareTo(BigDecimal.ZERO) < 0) {
            return String.format("📉 Снижение %.2f руб. (%.1f%%) за %d дней", 
                change.doubleValue(), percentChange.doubleValue(), daysBetween);
        } else {
            return "➡️ Цена стабильна";
        }
    }
    
    //тут работа с bigdec
    private String calculatePriceChange(BigDecimal previousPrice, BigDecimal currentPrice) {
        BigDecimal change = currentPrice.subtract(previousPrice);
        BigDecimal percent = change.divide(previousPrice, 4, RoundingMode.HALF_UP)
                                 .multiply(BigDecimal.valueOf(100));
        
        //тут также есть различия в зависимости от значения
        if (change.compareTo(BigDecimal.ZERO) > 0) {
            return String.format("( ↑ +%.1f%%)", percent.doubleValue());
        } else if (change.compareTo(BigDecimal.ZERO) < 0) {
            return String.format("( ↓ %.1f%%)", percent.doubleValue());
        } else {
            return "(→ 0%)";
        }
    }
   
    @Transactional(readOnly = true)
    public List<String> getAllProductNames() {
        return purchaseDetailRepository.findDistinctProductNames();
    }
    
    // Вспомогательные методы для конвертации
    //разные методы для конвертации в подходящие классы для каждой из двух таблиц
    private PurchaseDetailEntity convertToPurchaseDetail(Product product) {
        return new PurchaseDetailEntity(
            product.getName(),
            product.getQuantity(),
            BigDecimal.valueOf(product.getTotalPrice()),
            product.getDate()
        );
    }
    
    private ProductStatEntity convertToProductStat(Product product) {
        return new ProductStatEntity(
            product.getName(),
            BigDecimal.valueOf(product.getPricePerUnit()),
            product.getDate()
        );
    }
}