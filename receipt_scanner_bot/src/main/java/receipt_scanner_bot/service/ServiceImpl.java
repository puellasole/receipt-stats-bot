package receipt_scanner_bot.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import receipt_scanner_bot.client.Client;
import receipt_scanner_bot.dto.ProductDetailStatsDTO;
import receipt_scanner_bot.dto.ProductStatsDTO;
import receipt_scanner_bot.formatter.StatsFormatter;
import receipt_scanner_bot.model.Product;
import receipt_scanner_bot.parser.Parser;

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
    public String getStatsForAllProducts(Long chatId) {
    	List<ProductStatsDTO> stats = databaseService.getStatsForAllProducts(chatId);
        return statsFormatter.formatAllProductsStats(stats);
    }
    
    @Override
    public String getStatsForOneProduct(Long chatId, String productName) {
    	Optional<ProductDetailStatsDTO> stats = databaseService.getStatsForProduct(chatId, productName);
        return stats.map(statsFormatter::formatProductDetailStats)
                   .orElse("❌ Продукт '" + productName + "' не найден в базе данных.\n\n" +
                          "💡 Попробуйте поискать похожие названия или проверьте правильность написания.");
    }
    
    @Override
    public String uploadReceipt(Long chatId, String checkqrcode) {
    	
    	try {
    		System.out.println("Вошли в метод аплоуд");
            String responsexml = client.getReceiptData(checkqrcode);
            System.out.println("Строка ответа с сервера" + responsexml);
            List<Product> products = parser.parse(responsexml);
            for(Product p : products) {
            	System.out.println(p.toString());
            }
            System.out.println("Конвертация выполнена");
            databaseService.saveProducts(chatId, products);
            System.out.println("Продукты сохранены");
            return statsFormatter.formatProducts(products);
            
        } catch (IOException e) {
            return "❌ Ошибка при обработке чека: " + e.getMessage();
        }
    }
    
}
