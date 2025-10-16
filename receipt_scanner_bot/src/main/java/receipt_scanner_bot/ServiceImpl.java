package receipt_scanner_bot;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ServiceImpl implements ReceiptService {
    
    private final Client client;
    private final Parser parser;
    private final DatabaseService databaseService;
    private final StatsFormatter statsFormatter;
    
    // Конструктор с dependency injection
    //why are constractors different
    public ServiceImpl(Client client, Parser parser, DatabaseService databaseService, StatsFormatter statsFormatter) {
        this.client = client;
        this.parser = parser;
        this.databaseService = databaseService;
        this.statsFormatter = statsFormatter;
    }
    
    @Override
    public String getStatsForAllProducts() {
        return databaseService.getStatsForAllProducts();
    }
    
    @Override
    public String getStatsForOneProduct(String productname) {
        return databaseService.getStatsForProduct(productname);
    }
    
    @Override
    public String uploadReceipt(String checkqrcode) {
        String productsAsString = "";
        try {
            String responsexml = client.getReceiptData(checkqrcode);
            List<Product> products = parser.parse(responsexml);
            
            // Сохраняем в базу данных
            databaseService.saveProducts(products);
            
            //вынести в другой класс
            productsAsString = statsFormatter.formatProducts(products);
            //должна ли я просто вызывать метод, он же возвращает стрингу
            
            
        } catch (IOException e) {
            e.printStackTrace();
            return "❌ Ошибка при обработке чека: " + e.getMessage();
        }
        return productsAsString;
    }
    /*
    public String printProducts(List<Product> products) {
        StringBuilder sb = new StringBuilder();
        sb.append("✅ Чек успешно обработан и сохранен!\n\n");
        sb.append("📦 Купленные продукты:\n");
        
        for(Product p : products) {
            sb.append("• ");
            sb.append(p.toString());
            sb.append("\n");
        }
        
        sb.append("\n💾 Данные сохранены в базу для статистики.");
        return sb.toString();
    }*/
}
