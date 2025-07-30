package kr.hhplus.be.server.product.domain.repository;

import kr.hhplus.be.server.product.domain.entity.ProductViewCountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductViewCountRepository extends JpaRepository<ProductViewCountEntity, Long> {

    Optional<ProductViewCountEntity> findByProductId(Long productId);
}