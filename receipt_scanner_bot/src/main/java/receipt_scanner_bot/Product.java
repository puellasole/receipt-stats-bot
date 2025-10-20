package receipt_scanner_bot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Product {
	
	private String name;
    private BigDecimal quantity;
    private BigDecimal pricePerUnit; 
    private BigDecimal totalPrice; 
    private LocalDate date;
    private boolean isWeightProduct;

    public Product(String name,
    		BigDecimal quantity,
    		BigDecimal pricePerUnit,
    		BigDecimal totalPrice,
    		LocalDate date) {
        this.name = name;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.pricePerUnit = pricePerUnit;
        this.date = date;
        this.isWeightProduct = isWeightProduct();
    }

    public String getName() { return name; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public LocalDate getDate() { return date; }
    public boolean getIsWeightProduct() { return isWeightProduct; }

    public boolean isWeightProduct() {
        // Проверяем есть ли дробная часть
        return quantity.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0;
    }
    
    @Override
    public String toString() {
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = date.format(formatter);
        
        // Ограничиваем название продукта до 2 слов
        String limitedName = Utils.limitWords(name, 2);
        String formattedQuantity = isWeightProduct ? "кг." : "шт.";
        
        String quantityValue = isWeightProduct() ? 
        	    String.format("%.3f", quantity.doubleValue()) : 
        	    String.valueOf(quantity.intValue());
        
        return String.format("%s: %s %s х %.2f руб = %.2f руб, дата: %s", 
        		limitedName,
        		quantityValue,
        		formattedQuantity,
        		pricePerUnit.doubleValue(),
        		totalPrice.doubleValue(),
        		formattedDate);
    }

}
