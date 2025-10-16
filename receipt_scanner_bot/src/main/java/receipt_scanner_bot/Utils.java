package receipt_scanner_bot;

public class Utils {
	
	public static String limitWords(String text, int maxWords) {
        String[] words = text.split("\\s+");
        if (words.length <= maxWords) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < maxWords; i++) {
            if (i > 0) result.append(" ");
            result.append(words[i]);
        }
        return result.toString();
    }

}
