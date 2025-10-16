package receipt_scanner_bot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class Parser {
	List<Product> parse(String jsonToParse){
		final DateTimeFormatter API_DATE_FORMATTER = 
    	        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        List<Product> products = new ArrayList<>();
        
        JSONObject root = new JSONObject(jsonToParse);
        JSONObject data = root.getJSONObject("data");
        JSONObject json = data.getJSONObject("json");
        JSONArray items = json.getJSONArray("items");
        
        String dateTimeStr = json.getString("dateTime");
        LocalDateTime purchaseDate = LocalDateTime.parse(dateTimeStr, API_DATE_FORMATTER);
        LocalDate date = purchaseDate.toLocalDate();
        
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            
            String name = item.getString("name");
            int quantity = item.getInt("quantity");
            double pricePerUnit = item.getDouble("price") / 100.0;
            double totalPrice = item.getDouble("sum") / 100.0;
                        
            products.add(new Product(name, quantity, pricePerUnit, totalPrice, date));
        }
        
        return products;
	}
	
}
