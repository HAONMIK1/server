package kr.hhplus.be.server.product.domain.repository;

import kr.hhplus.be.server.product.domain.entity.ProductEntity;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    List<ProductEntity> findAll();
    Optional<ProductEntity> findById(Long id);
    ProductEntity save(ProductEntity product);
    void updateStock(Long productId, int quantity);
    Optional<ProductEntity> findByIdWithLock(Long productId);
}