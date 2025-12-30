package receipt_scanner_bot.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import receipt_scanner_bot.entities.ProductStatEntity;

@Repository
public interface ProductStatRepository extends JpaRepository<ProductStatEntity, Long> {
	
	List<ProductStatEntity> findByChatIdAndProductNameOrderByStatDateAsc(Long chatId, String productName);
	
    
    @Query("SELECT ps FROM ProductStatEntity ps WHERE ps.chatId = :chatId AND ps.productName = :productName " +
           "ORDER BY ps.statDate DESC LIMIT 1")
    ProductStatEntity findLatestByChatIdAndProductName(@Param("chatId") Long chatId, @Param("productName") String productName);

    @Modifying
    @Query("DELETE FROM ProductStatEntity ps WHERE ps.chatId = :chatId AND ps.productName = :productName")
    void deleteByChatIdAndProductName(@Param("chatId") Long chatId, @Param("productName") String productName);
}

