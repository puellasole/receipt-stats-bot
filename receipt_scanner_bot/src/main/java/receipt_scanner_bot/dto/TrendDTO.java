package receipt_scanner_bot.dto;

import java.math.BigDecimal;

public record TrendDTO(
	    BigDecimal changeAmount,
	    BigDecimal changePercent,
	    long daysBetween,
	    String direction
	) {}
