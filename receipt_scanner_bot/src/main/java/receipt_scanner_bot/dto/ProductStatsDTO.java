package receipt_scanner_bot.dto;

import java.math.BigDecimal;

public record ProductStatsDTO(
		//тут не считает дни
	    String productName,
	    int totalQuantity,
	    BigDecimal totalAmount
	) {}


