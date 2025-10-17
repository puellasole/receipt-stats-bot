package receipt_scanner_bot.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProductDetailStatsDTO(
	    String productName,
	    BigDecimal minPrice,
	    BigDecimal maxPrice,
	    BigDecimal averagePrice,
	    BigDecimal priceSpread,
	    String trend,
	    int recordCount,
	    LocalDate firstDate,
	    LocalDate lastDate,
	    List<PriceHistoryDTO> priceHistory
	) {}
