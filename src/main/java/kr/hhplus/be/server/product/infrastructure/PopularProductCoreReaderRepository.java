package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.entity.PopularProductEntity;
import kr.hhplus.be.server.product.domain.repository.PopularProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PopularProductCoreReaderRepository implements PopularProductRepository {
    
    private final PopularProductJpaRepository popularProductJpaRepository;
    
    @Override
    public List<Object[]> findPopularProducts() {
        return popularProductJpaRepository.findPopularProducts();
    }
    
    @Override
    public List<PopularProductEntity> findPopularProductsOrderedByPriority() {
        return popularProductJpaRepository.findPopularProductsOrderedByPriority();
    }
    
    @Override
    public void deleteAllPopularProducts() {
        popularProductJpaRepository.deleteAllPopularProducts();
    }
    
    @Override
    public PopularProductEntity save(PopularProductEntity popularProduct) {
        return popularProductJpaRepository.save(popularProduct);
    }
}