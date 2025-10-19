package receipt_scanner_bot;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseDetailRepository extends JpaRepository<PurchaseDetailEntity, Long> {
    /*
    List<PurchaseDetailEntity> findAllByOrderByPurchaseDateAscProductNameAsc();
    
    @Query("SELECT DISTINCT p.productName FROM PurchaseDetailEntity p")
    List<String> findDistinctProductNames();
    
    List<PurchaseDetailEntity> findByProductNameContainingIgnoreCase(String productName);
	*/
	List<PurchaseDetailEntity> findByChatIdOrderByPurchaseDateAscProductNameAsc(Long chatId);
    
    @Query("SELECT DISTINCT p.productName FROM PurchaseDetailEntity p WHERE p.chatId = :chatId")
    List<String> findDistinctProductNamesByChatId(@Param("chatId") Long chatId);
    
    List<PurchaseDetailEntity> findByChatIdAndProductNameContainingIgnoreCase(Long chatId, String productName);
}
