package receipt_scanner_bot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

@Configuration
public class Config {
	//is there any reasons to add interceptor
	//are there ways with annotation?
	private HttpLoggingInterceptor log = new HttpLoggingInterceptor().setLevel(Level.BASIC);
	
	@Bean
	public OkHttpClient okHttpClient() {
		return new OkHttpClient.Builder()
				.addInterceptor(log)
				.build();
	}
	
	@Bean
	public TelegramBotsApi telegramBotsApi(Bot bot) throws TelegramApiException{
		var api = new TelegramBotsApi(DefaultBotSession.class);
		api.registerBot(bot);
		return api;
	}
	

}
