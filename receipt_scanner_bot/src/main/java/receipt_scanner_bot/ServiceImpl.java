package receipt_scanner_bot;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import receipt_scanner_bot.dto.ProductDetailStatsDTO;
import receipt_scanner_bot.dto.ProductStatsDTO;

@Service
public class ServiceImpl implements ReceiptService {
    
    private final Client client;
    private final Parser parser;
    private final DatabaseService databaseService;
    private final StatsFormatter statsFormatter;
    
    public ServiceImpl(Client client, Parser parser, DatabaseService databaseService, StatsFormatter statsFormatter) {
        this.client = client;
        this.parser = parser;
        this.databaseService = databaseService;
        this.statsFormatter = statsFormatter;
    }
    
    @Override
    public String getStatsForAllProducts() {
    	List<ProductStatsDTO> stats = databaseService.getStatsForAllProducts();
        return statsFormatter.formatAllProductsStats(stats);
    }
    
    @Override
    public String getStatsForOneProduct(String productName) {
    	Optional<ProductDetailStatsDTO> stats = databaseService.getStatsForProduct(productName);
        return stats.map(statsFormatter::formatProductDetailStats)
                   .orElse("❌ Продукт '" + productName + "' не найден в базе данных.\n\n" +
                          "💡 Попробуйте поискать похожие названия или проверьте правильность написания.");
    }
    
    @Override
    public String uploadReceipt(String checkqrcode) {
    	
    	try {
            String responsexml = client.getReceiptData(checkqrcode);
            List<Product> products = parser.parse(responsexml);
            
            databaseService.saveProducts(products);
            return statsFormatter.formatProducts(products);
            
        } catch (IOException e) {
            return "❌ Ошибка при обработке чека: " + e.getMessage();
        }
    }
    
}
