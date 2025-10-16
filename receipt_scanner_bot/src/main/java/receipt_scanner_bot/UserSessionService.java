package receipt_scanner_bot;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserSessionService {
    
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    
    public void setUserState(Long chatId, UserState state) {
        userStates.put(chatId, state);
    }
    
    public UserState getUserState(Long chatId) {
        return userStates.getOrDefault(chatId, UserState.DEFAULT);
    }
    
    public void clearUserState(Long chatId) {
        userStates.remove(chatId);
    }
}

