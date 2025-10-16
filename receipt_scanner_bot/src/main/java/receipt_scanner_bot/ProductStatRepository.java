package receipt_scanner_bot;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductStatRepository extends JpaRepository<ProductStatEntity, Long> {
    
    List<ProductStatEntity> findByProductNameOrderByStatDateAsc(String productName);
    
    @Query("SELECT ps FROM ProductStatEntity ps WHERE ps.productName = :productName " +
           "ORDER BY ps.statDate DESC LIMIT 1")
    ProductStatEntity findLatestByProductName(@Param("productName") String productName);
}

