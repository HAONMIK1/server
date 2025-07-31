package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    @Modifying
    @Query("UPDATE ProductEntity p SET p.stockQuantity = p.stockQuantity - :quantity WHERE p.id = :productId")
    void updateStock(@Param("productId") Long productId, @Param("quantity") int quantity);
    
    @Query("SELECT p FROM ProductEntity p WHERE p.id = :productId")
    Optional<ProductEntity> findByIdWithLock(@Param("productId") Long productId);
}