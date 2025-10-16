package receipt_scanner_bot;

interface ReceiptService {

	String getStatsForAllProducts();
	String getStatsForOneProduct(String productname);
	String uploadReceipt(String checkqrcode);
}
