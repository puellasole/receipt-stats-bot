package receipt_scanner_bot;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class StatsFormatter {
	
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

