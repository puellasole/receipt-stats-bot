package receipt_scanner_bot.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PriceHistoryDTO(
	    LocalDate date,
	    BigDecimal price,
	    String change
	) {}
