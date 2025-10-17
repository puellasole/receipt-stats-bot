package receipt_scanner_bot;

import java.util.List;

import org.springframework.stereotype.Component;

import receipt_scanner_bot.dto.PriceHistoryDTO;
import receipt_scanner_bot.dto.ProductDetailStatsDTO;
import receipt_scanner_bot.dto.ProductStatsDTO;

@Component
public class StatsFormatter {
	
	public String formatAllProductsStats(List<ProductStatsDTO> stats) {
        if (stats.isEmpty()) {
            return "📊 База данных пуста. Добавьте сначала несколько чеков.";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("📊 ОБЩАЯ СТАТИСТИКА ПО ВСЕМ ПРОДУКТАМ:\n\n");
        
        int rank = 1;
        for (ProductStatsDTO stat : stats) {
            result.append(String.format("%d.  →  %.2f ₽    (%d шт.)\n", 
                rank, stat.totalAmount().doubleValue(), stat.totalQuantity()));
            result.append(String.format("   %s\n\n", stat.productName()));
            rank++;
        }
        
        return result.toString();
    }
    
    public String formatProductDetailStats(ProductDetailStatsDTO stats) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("📊 СТАТИСТИКА: %s\n\n", stats.productName().toUpperCase()));
        
        // Диапазон цен
        result.append("💰 Диапазон цен: ");
        result.append(String.format("%.2f - %.2f руб.\n", 
            stats.minPrice().doubleValue(), stats.maxPrice().doubleValue()));
        result.append(String.format("   • Средняя: %.2f руб.\n", stats.averagePrice().doubleValue()));
        result.append(String.format("   • Разброс: %.2f руб.\n\n", stats.priceSpread().doubleValue()));
        
        // Общая информация
        result.append(" Общий тренд:\n");
        result.append(stats.trend() + "\n");
        result.append(String.format("   • Количество записей: %d\n", stats.recordCount()));
        result.append(String.format("   • Период наблюдений: %s - %s\n\n", 
            stats.firstDate(), stats.lastDate()));
        
        // Последние цены
        result.append("🔄 Последние изменения цен:\n");
        int displayCount = Math.min(stats.priceHistory().size(), 5);
        List<PriceHistoryDTO> recentPrices = stats.priceHistory().subList(
            stats.priceHistory().size() - displayCount, stats.priceHistory().size());
        
        for (PriceHistoryDTO price : recentPrices) {
            result.append(String.format("   • %s: %.2f руб. %s\n", 
                price.date(), price.price(), price.change()));
        }
        
        return result.toString();
    }
	
    public String formatProducts(List<Product> products) {
        StringBuilder sb = new StringBuilder();
        sb.append("✅ Чек успешно обработан и сохранен!\n\n");
        sb.append("📦 Купленные продукты:\n");
        
        for (Product p : products) {
            sb.append("• ");
            sb.append(p.toString());
            sb.append("\n");
        }
        
        sb.append("\n💾 Данные сохранены в базу для статистики.");
        return sb.toString();
    }
    
}

