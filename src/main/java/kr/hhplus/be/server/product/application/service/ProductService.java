package kr.hhplus.be.server.product.application.service;

import kr.hhplus.be.server.product.domain.entity.PopularProductEntity;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import kr.hhplus.be.server.product.domain.entity.ProductSalesCountEntity;
import kr.hhplus.be.server.product.domain.entity.ProductViewCountEntity;
import kr.hhplus.be.server.product.domain.repository.PopularProductRepository;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.product.domain.repository.ProductSalesCountRepository;
import kr.hhplus.be.server.product.domain.repository.ProductViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final PopularProductRepository popularProductRepository;
    private final ProductViewCountRepository productViewCountRepository;
    private final ProductSalesCountRepository productSalesCountRepository;

    @Transactional
    public List<ProductEntity> getProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public ProductEntity getProduct(Long productId) {
        ProductEntity product = getProductInternal(productId);

        increaseViewCount(productId);

        return product;
    }

    @Transactional
    private ProductEntity getProductInternal(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
    }

    @Transactional
    public void increaseViewCount(Long productId) {
        ProductViewCountEntity viewCount = productViewCountRepository.findByProductId(productId)
                .orElseGet(() -> {
                    ProductViewCountEntity newViewCount = new ProductViewCountEntity();
                    newViewCount.setProductId(productId);
                    newViewCount.setViewCount(0);
                    return newViewCount;
                });

        viewCount.setViewCount(viewCount.getViewCount() + 1);
        productViewCountRepository.save(viewCount);
    }

    @Transactional
    public void checkStock(Long productId, int quantity) {
        ProductEntity product = getProductInternal(productId);
        if (!product.canPurchase(quantity)) {
            throw new IllegalArgumentException("상품 재고가 부족합니다.");
        }
    }

    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        ProductEntity product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        product.decreaseStock(quantity);
        productRepository.save(product);
        
        // 판매량 증가
        increaseSalesCount(productId);
    }

    @Transactional
    public void increaseSalesCount(Long productId) {
        ProductSalesCountEntity salesCount = productSalesCountRepository.findByProductId(productId)
                .orElseGet(() -> {
                    ProductSalesCountEntity newSalesCount = new ProductSalesCountEntity();
                    newSalesCount.setProductId(productId);
                    newSalesCount.setSalesCount(0);
                    return newSalesCount;
                });

        salesCount.setSalesCount(salesCount.getSalesCount() + 1);
        productSalesCountRepository.save(salesCount);
    }

    @Transactional
    public List<PopularProductEntity> getPopularProducts() {
        return popularProductRepository.findPopularProductsOrderedByPriority();
    }

    //상위 5개만 인기상품 테이블에 저장
    @Transactional
    public void updatePopularProducts() {
        // 1. 기존 인기상품 테이블 초기화
        popularProductRepository.deleteAllPopularProducts();

        // 2. 상품 메인 테이블과 판매량, 조회수 테이블 LEFT JOIN하여 상위 5개 조회
        List<Object[]> top5Products = popularProductRepository.findPopularProducts();

        // 3. 인기상품 테이블에 저장
        for (Object[] data : top5Products) {
            Long productId = ((Number) data[0]).longValue();
            int salesCount = ((Number) data[1]).intValue();
            int viewCount = ((Number) data[2]).intValue();

            PopularProductEntity popularProduct = PopularProductEntity.createPopularProduct(
                    productId, viewCount, salesCount
            );
            popularProductRepository.save(popularProduct);
        }
    }
}
