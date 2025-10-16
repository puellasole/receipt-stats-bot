package receipt_scanner_bot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Product {
	
	private String name;
    private int quantity;
    private double pricePerUnit; 
    private double totalPrice; 
    private LocalDate date;

    public Product(String name, int quantity, double pricePerUnit, double totalPrice, LocalDate date) {
        this.name = name;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.pricePerUnit = calculatePricePerUnit();
        this.date = date;
    }

    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getPricePerUnit() { return pricePerUnit; }
    public double getTotalPrice() { return totalPrice; }
    public LocalDate getDate() { return date; }
    
    private double calculatePricePerUnit() {
    	return totalPrice / quantity;
    }
    
    @Override
    public String toString() {
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = date.format(formatter);
        
        // Ограничиваем название продукта до 2 слов
        String limitedName = Utils.limitWords(name, 2);
        
        return String.format("%s: %d шт. х %.2f руб = %.2f руб, дата: %s", 
        		limitedName, quantity, pricePerUnit, totalPrice, formattedDate);
    }

}
