package receipt_scanner_bot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "purchase_details")
public class PurchaseDetailEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "chat_id", nullable = false)
    private Long chatId;
    
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;
    
    @Column(name = "is_weight_product", nullable = false)
    private boolean isWeightProduct;
    
    public PurchaseDetailEntity() {}
    public PurchaseDetailEntity(Long chatId,
    		String productName,
    		BigDecimal quantity, 
            BigDecimal price,
            LocalDate purchaseDate,
            boolean isWeightProduct) {
    	this.chatId = chatId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.purchaseDate = purchaseDate;
        this.isWeightProduct = isWeightProduct;
    }
    
    public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public BigDecimal getQuantity() { return quantity; }
    public void setInteger(BigDecimal quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    
    public boolean isWeightProduct() { return isWeightProduct; }
	public void setWeightProduct(boolean isWeightProduct) { this.isWeightProduct = isWeightProduct; }

}