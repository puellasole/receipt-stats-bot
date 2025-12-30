package receipt_scanner_bot.service;

public interface ReceiptService {

	String getStatsForAllProducts(Long chatId);
	String getStatsForOneProduct(Long chatId, String productname);
	String deleteProduct(Long chatId, String productname);
	String uploadReceipt(Long chatId, String checkqrcode);
}
