package receipt_scanner_bot;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import receipt_scanner_bot.dto.PriceHistoryDTO;
import receipt_scanner_bot.dto.ProductDetailStatsDTO;
import receipt_scanner_bot.dto.ProductStatsDTO;
import receipt_scanner_bot.dto.TrendDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    public void saveProducts(Long chatId, List<Product> products) {
    	
        try {
            List<PurchaseDetailEntity> purchaseDetails = new ArrayList<>();
            List<ProductStatEntity> productStats = new ArrayList<>();
            
            for (Product product : products) {
                purchaseDetails.add(convertToPurchaseDetail(chatId, product));
                productStats.add(convertToProductStat(chatId, product));
            }
            
            purchaseDetailRepository.saveAll(purchaseDetails);
            productStatRepository.saveAll(productStats);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to save products", e);
        }
    }
    
    @Transactional(readOnly = true)
    public List<ProductStatsDTO> getStatsForAllProducts(Long chatId) {
        List<PurchaseDetailEntity> allPurchases = purchaseDetailRepository
        		.findByChatIdOrderByPurchaseDateAscProductNameAsc(chatId);
        
        if (allPurchases.isEmpty()) {
            return List.of(); // Возвращаем пустой список вместо строки
        }
        
        return allPurchases.stream()
            .collect(Collectors.groupingBy(
                PurchaseDetailEntity::getProductName,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> {
                    	BigDecimal totalQuantity = list.stream()
                    			.map(PurchaseDetailEntity::getQuantity)
                    			.reduce(BigDecimal.ZERO, BigDecimal::add);
                    	
                        BigDecimal totalAmount = list.stream()
                            .map(PurchaseDetailEntity::getPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                        
                        boolean isWeightProduct = list.get(0).isWeightProduct();
                        
                        return new ProductStatsDTO(
                            list.get(0).getProductName(), // Берем имя из любой записи
                            totalQuantity, 
                            totalAmount,
                            isWeightProduct
                        );
                    }
                )
            ))
            .values().stream()
            .sorted((p1, p2) -> p2.totalAmount().compareTo(p1.totalAmount()))
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Optional<ProductDetailStatsDTO> getStatsForProduct(Long chatId, String productName) {
        List<ProductStatEntity> stats = productStatRepository
        		.findByChatIdAndProductNameOrderByStatDateAsc(chatId, productName);
        
        if (stats.isEmpty()) {
            return Optional.empty();
        }
        
        // Вычисления статистики
        BigDecimal minPrice = stats.stream()
            .map(ProductStatEntity::getUnitPrice)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
            
        BigDecimal maxPrice = stats.stream()
            .map(ProductStatEntity::getUnitPrice)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
            
        BigDecimal averagePrice = stats.stream()
            .map(ProductStatEntity::getUnitPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(stats.size()), 2, RoundingMode.HALF_UP);
        
        TrendDTO trend = calculateTrend(stats);
        List<PriceHistoryDTO> priceHistory = buildPriceHistory(stats);
        
        return Optional.of(new ProductDetailStatsDTO(
            productName,
            minPrice,
            maxPrice,
            averagePrice,
            maxPrice.subtract(minPrice),
            formatTrend(trend), // Только форматирование строки
            stats.size(),
            stats.get(0).getStatDate(),
            stats.get(stats.size()-1).getStatDate(),
            priceHistory
        ));
    }
    
    // Логика вычисления тренда (возвращает DTO, а не строку)
    private TrendDTO calculateTrend(List<ProductStatEntity> stats) {
        if (stats.size() < 2) {
            return new TrendDTO(BigDecimal.ZERO, BigDecimal.ZERO, 0, "STABLE");
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
        
        String direction = change.compareTo(BigDecimal.ZERO) > 0 ? "UP" : 
                          change.compareTo(BigDecimal.ZERO) < 0 ? "DOWN" : "STABLE";
        
        return new TrendDTO(change, percentChange, daysBetween, direction);
    }
    
    // Логика построения истории цен
    private List<PriceHistoryDTO> buildPriceHistory(List<ProductStatEntity> stats) {
        List<PriceHistoryDTO> history = new ArrayList<>();
        
        for (int i = 0; i < stats.size(); i++) {
            ProductStatEntity current = stats.get(i);
            String change = i > 0 ? calculatePriceChange(stats.get(i-1).getUnitPrice(), current.getUnitPrice()) : "";
            
            history.add(new PriceHistoryDTO(
                current.getStatDate(),
                current.getUnitPrice(),
                change
            ));
        }
        
        return history;
    }
    
    // Логика расчета изменения цены
    private String calculatePriceChange(BigDecimal previousPrice, BigDecimal currentPrice) {
        if (previousPrice.compareTo(BigDecimal.ZERO) == 0) return "";
        
        BigDecimal change = currentPrice.subtract(previousPrice);
        BigDecimal percent = change.divide(previousPrice, 4, RoundingMode.HALF_UP)
                                 .multiply(BigDecimal.valueOf(100));
        
        if (change.compareTo(BigDecimal.ZERO) > 0) {
            return String.format("( ↑ +%.1f%%)", percent.doubleValue());
        } else if (change.compareTo(BigDecimal.ZERO) < 0) {
            return String.format("( ↓ %.1f%%)", percent.doubleValue());
        } else {
            return "(→ 0%)";
        }
    }
    
    // Только форматирование тренда в строку
    private String formatTrend(TrendDTO trend) {
        return switch (trend.direction()) {
            case "UP" -> String.format("📈 Рост +%.2f руб. (+%.1f%%) за %d дней", 
                trend.changeAmount().doubleValue(), trend.changePercent().doubleValue(), trend.daysBetween());
            case "DOWN" -> String.format("📉 Снижение %.2f руб. (%.1f%%) за %d дней", 
                trend.changeAmount().doubleValue(), trend.changePercent().doubleValue(), trend.daysBetween());
            default -> "➡️ Цена стабильна";
        };
    }
    
    // Вспомогательные методы для конвертации
    //разные методы для конвертации в подходящие классы для каждой из двух таблиц
    private PurchaseDetailEntity convertToPurchaseDetail(Long chatId, Product product) {
        return new PurchaseDetailEntity(
        	chatId,
            product.getName(),
            product.getQuantity(),
            product.getTotalPrice(),
            product.getDate(),
            product.getIsWeightProduct()
        );
    }
    
    private ProductStatEntity convertToProductStat(Long chatId, Product product) {
        return new ProductStatEntity(
        	chatId,
            product.getName(),
            product.getPricePerUnit(),
            product.getDate()
        );
    }
}