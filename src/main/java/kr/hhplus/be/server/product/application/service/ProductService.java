package kr.hhplus.be.server.product.application.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import kr.hhplus.be.server.product.domain.entity.PopularProductEntity;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import kr.hhplus.be.server.product.domain.entity.ProductSalesCountEntity;
import kr.hhplus.be.server.product.domain.entity.ProductViewCountEntity;
import kr.hhplus.be.server.product.domain.repository.PopularProductRepository;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.product.domain.repository.ProductSalesCountRepository;
import kr.hhplus.be.server.product.domain.repository.ProductViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final PopularProductRepository popularProductRepository;
    private final ProductViewCountRepository productViewCountRepository;
    private final ProductSalesCountRepository productSalesCountRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String SALES_RANKING_KEY = "product:sales:ranking";
    
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

        // Redis 판매량 랭킹 업데이트
        incrementSalesRanking(productId, 1);
    }

    @Cacheable(value = "popular-products")
    @Transactional
    public List<PopularProductEntity> getPopularProducts() {
        return popularProductRepository.findPopularProductsOrderedByPriority();
    }


    @CacheEvict(value = "popular-products", allEntries = true)
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

    public List<ProductSalesCountEntity> getRankingProducts() {
        try {
            // Redis SortedSet에서 판매량 높은 순으로 상위 10개 상품ID와 점수 조회
            Set<ZSetOperations.TypedTuple<String>> topProductsWithScores = redisTemplate.opsForZSet()
                    .reverseRangeWithScores(SALES_RANKING_KEY, 0, 9);

            // Redis에 데이터가 없으면 DB에서 로드 후 다시 조회
            if (topProductsWithScores == null || topProductsWithScores.isEmpty()) {
                loadProductsToRedis();

                topProductsWithScores = redisTemplate.opsForZSet()
                        .reverseRangeWithScores(SALES_RANKING_KEY, 0, 9);
            }
            // 상위 10개 상품을 ProductSalesCountEntity로 변환
            List<ProductSalesCountEntity> rankingProducts = new ArrayList<>();

            for (ZSetOperations.TypedTuple<String> productTuple : topProductsWithScores) {
                String productIdStr = productTuple.getValue();
                Double salesScore = productTuple.getScore();

                Long productId = Long.valueOf(productIdStr);

                int salesCount = salesScore != null ? salesScore.intValue() : 0;

                ProductSalesCountEntity rankingProduct = new ProductSalesCountEntity();
                rankingProduct.setProductId(productId);
                rankingProduct.setSalesCount(salesCount);

                rankingProducts.add(rankingProduct);
            }

            return rankingProducts;
        } catch (Exception e) {
            System.err.println("Redis에서 상품 데이터 조회 실패: " + e.getMessage());
            return List.of();
        }
    }

    public void incrementSalesRanking(Long productId, int quantity) {
        try {
            // 1. Redis에서 현재 상품의 판매량 점수 조회
            String productIdStr = productId.toString();
            Double currentSalesScore = redisTemplate.opsForZSet().score(SALES_RANKING_KEY, productIdStr);

            // 2. 새로운 판매량 점수 계산
            double newSalesScore = quantity;
            if (currentSalesScore != null) {
                newSalesScore = currentSalesScore + quantity;
            }

            // 3. Redis SortedSet에 상품ID를 키로, 판매량을 점수로 저장
            redisTemplate.opsForZSet().add(SALES_RANKING_KEY, productIdStr, newSalesScore);
        } catch (Exception e) {
            System.err.println("Redis 판매량 랭킹 업데이트 실패 - 상품ID: " + productId +
                    ", 수량: " + quantity +
                    ", 에러: " + e.getMessage());
        }
    }
    public void loadProductsToRedis() {
        try {
            List<ProductSalesCountEntity> allProducts = productRepository.findProductsBySales();

            for (ProductSalesCountEntity product : allProducts) {
                Long productId = product.getProductId();
                Integer salesCount = product.getSalesCount();

                redisTemplate.opsForZSet().add(SALES_RANKING_KEY, productId.toString(), salesCount);
            }

        } catch (Exception e) {
            System.err.println("Redis에 상품 데이터 로드 실패: " + e.getMessage());
        }
    }
}
