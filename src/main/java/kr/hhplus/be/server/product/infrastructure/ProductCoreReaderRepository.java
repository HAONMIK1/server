package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductCoreReaderRepository implements ProductRepository {
    
    private final ProductJpaRepository productJpaRepository;
    
    @Override
    public List<ProductEntity> findAll() {
        return productJpaRepository.findAll();
    }
    
    @Override
    public Optional<ProductEntity> findById(Long id) {
        return productJpaRepository.findById(id);
    }
    
    @Override
    public ProductEntity save(ProductEntity product) {
        return productJpaRepository.save(product);
    }
    
    @Override
    public void updateStock(Long productId, int quantity) {
        productJpaRepository.updateStock(productId, quantity);
    }
}