package receipt_scanner_bot.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import receipt_scanner_bot.model.UserState;
import receipt_scanner_bot.service.ReceiptService;
import receipt_scanner_bot.service.UserSessionService;

@Component
public class Bot extends TelegramLongPollingBot {
	
	private static Logger log = LoggerFactory.getLogger(Bot.class);
	
	private static final String START = "/start";
	private static final String RECEIPT_UPLOAD = "/receipt_upload";
	private static final String STATS = "/stats";
	private static final String HELP = "/help";
	private static final String PRODUCT_STATS = "/product_stats";
	
	@Autowired
	private ReceiptService service;
	
	@Autowired
    private UserSessionService userSessionService;
	
	public Bot(@Value("${bot.token}") String botToken) {
		super(botToken);
	}

	@Override
	public void onUpdateReceived(Update update) {
		
		if(!update.hasMessage() || !update.getMessage().hasText()) {
			return;
		}
		var message = update.getMessage().getText();
		var chatId = update.getMessage().getChatId();
		var userState = userSessionService.getUserState(chatId);
        		
        if (userState == UserState.WAITING_FOR_RECEIPT) {
            processReceiptQr(chatId, message);
            return;
        }
        
        if (userState == UserState.WAITING_FOR_PRODUCT) {
            processProductStat(chatId, message);
            return;
        }
        
		switch(message) {
			case START -> {
				String userName = update.getMessage().getChat().getFirstName();
				startCommand(chatId, userName);
			}
			case RECEIPT_UPLOAD -> {
				uploadCommand(chatId);
			}
			case STATS -> {
				statsCommand(chatId);
			}
			case PRODUCT_STATS -> {
				productCommand(chatId);
			}
			case HELP -> helpCommand(chatId);
			default -> unknownCommand(chatId);
		}
	}
	
	private void processProductStat(Long chatId, String productName) {
		try {
	        String result = service.getStatsForOneProduct(chatId, productName);
	        sendMessage(chatId, result);
	    } catch (Exception e) {
	        sendMessage(chatId, "❌ Ошибка при обработке продукта: " + e.getMessage());
	    } finally {
	        userSessionService.clearUserState(chatId);
	    }
		/*
		try {
			userSessionService.clearUserState(chatId);
			String result = service.getStatsForOneProduct(chatId, productName);
			sendMessage(chatId, result);
		} catch (Exception e) {
			sendMessage(chatId, "❌ Ошибка при обработке продукта: " + e.getMessage());
            userSessionService.clearUserState(chatId);
		}
		*/
	}
	
	private void processReceiptQr(Long chatId, String qrCode) {
		try {
	        // Обрабатываем QR-код
	        String result = service.uploadReceipt(chatId, qrCode);
	        sendMessage(chatId, result);
	        
	    } catch (Exception e) {
	        sendMessage(chatId, "❌ Ошибка при обработке чека: " + e.getMessage());
	    } finally {
	        // ⚡ Сбрасываем состояние ПОСЛЕ обработки!
	        userSessionService.clearUserState(chatId);
	    }
    }

	private void statsCommand(Long chatId) {
		
		String statres = service.getStatsForAllProducts(chatId);
		sendMessage(chatId, statres);
	}
	
	private void uploadCommand(Long chatId) {
		
		userSessionService.setUserState(chatId, UserState.WAITING_FOR_RECEIPT);
        sendMessage(chatId, "📷 Отправьте QR-код из чека в виде текста:");
	}
	
	private void productCommand(Long chatId) {
		
		userSessionService.setUserState(chatId, UserState.WAITING_FOR_PRODUCT);
        sendMessage(chatId, "📷 Напишите название продукта:");
	}
	
	private void unknownCommand(Long chatId) {
		var text = """ 
				Я не знаю эту команду :(
				""";
		sendMessage(chatId, text);
	}
	
	private void startCommand(Long chatId, String userName) {
		var text = """
				Добро пожаловать, %s!
				
				Доступные команды:
				/receipt_upload - Загрузить чек по QR-коду
				/stats - Общая статистика по всем продуктам
				/product_stats [название] - Детальная статистика по продукту
				/help - Подробная справка
				""";
		var formattedText = String.format(text, userName);
		sendMessage(chatId, formattedText);
		
	}
	private void sendMessage(Long chatId, String text) {
		var chatIdStr = String.valueOf(chatId);
		var sendMessage = new SendMessage(chatIdStr, text);
		try {
			execute(sendMessage);
		} catch (TelegramApiException e) {
			log.error("Failed sending message");
		}
		
	}
	@Override
	public String getBotUsername() {
		return "scanner_for_receipts_bot";
	}
		
	public void helpCommand(Long chatId) {
		String text = """
		        📖 Помощь по командам:
		        
		        /start - Начать работу
		        /receipt_upload - Загрузить чек по QR-коду
		        /stats - Общая статистика по всем продуктам
		        /product_stats [название] - Детальная статистика по конкретному продукту
		        /help - Показать эту справку
		        
		        💡 После загрузки чека данные автоматически сохраняются для анализа.
		        """;
		sendMessage(chatId, text);
	}
}
