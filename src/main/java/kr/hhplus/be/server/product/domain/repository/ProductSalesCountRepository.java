package kr.hhplus.be.server.product.domain.repository;

import kr.hhplus.be.server.product.domain.entity.ProductSalesCountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductSalesCountRepository extends JpaRepository<ProductSalesCountEntity, Long> {

    Optional<ProductSalesCountEntity> findByProductId(Long productId);
}