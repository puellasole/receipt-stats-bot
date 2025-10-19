package receipt_scanner_bot;

interface ReceiptService {

	String getStatsForAllProducts(Long chatId);
	String getStatsForOneProduct(Long chatId, String productname);
	String uploadReceipt(Long chatId, String checkqrcode);
}
