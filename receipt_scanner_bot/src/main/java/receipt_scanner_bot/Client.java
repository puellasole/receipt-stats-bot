package receipt_scanner_bot;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class Client {
	
	@Value("${fns.url}")
	private String URL;
	
	@Value("${fns.token}")
	private String token;
	
	@Autowired
	private OkHttpClient client; 
	
	public String getReceiptData(String qrRawData) throws IOException {
        // create multipart form data
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("qrraw", qrRawData)
            .addFormDataPart("token", token)
            .build();
        
        Request request = new Request.Builder()
            .url(URL)
            .header("Cookie", "ENGID=1.1")
            .post(requestBody)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ошибка запроса: " + response.code() + " " + response.message());
            }
            
            return response.body().string();
        }
    }
}
