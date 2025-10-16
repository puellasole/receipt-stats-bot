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
	//ADD THIS TO PROPERTIES
	private static final String URL = "https://proverkacheka.com/api/v1/check/get";
	
	@Autowired
	private OkHttpClient client; //how to get which okhttp client?
	
	@Value("${fns.token}")
	private String token;
	
	public String getReceiptData(String qrRawData) throws IOException {
        // create multipart form data
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("qrraw", qrRawData)
            .addFormDataPart("token", token)
            .build();
        
        // create request
        Request request = new Request.Builder()
            .url(URL)
            .header("Cookie", "ENGID=1.1")
            .post(requestBody)
            .build();
        
        // execute request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ошибка запроса: " + response.code() + " " + response.message());
            }
            
            return response.body().string();
        }
    }
}
