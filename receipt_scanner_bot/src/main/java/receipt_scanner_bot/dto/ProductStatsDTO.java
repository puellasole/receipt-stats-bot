package receipt_scanner_bot.dto;

import java.math.BigDecimal;

public record ProductStatsDTO(
	    String productName,
	    BigDecimal totalQuantity,
	    BigDecimal totalAmount,
	    boolean isWeightProduct
	) {}


