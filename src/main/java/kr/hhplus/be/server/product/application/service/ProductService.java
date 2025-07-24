package kr.hhplus.be.server.product.application.service;

import kr.hhplus.be.server.product.domain.entity.PopularProductEntity;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import kr.hhplus.be.server.product.domain.repository.PopularProductRepository;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final PopularProductRepository popularProductRepository;

    public List<ProductEntity> getProducts() {
        return productRepository.findAll();
    }

    public ProductEntity getProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
    }

    public boolean canPurchase(Long productId, Integer quantity) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        return product.canPurchase(quantity);
    }

    public ProductEntity updateStock(Long productId, Integer quantity) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        product.decreaseStock(quantity);
        return productRepository.save(product);
    }

    public void updateProductStatus(Long productId, ProductEntity.ProductStatus productStatus) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        product.updateStatus(productStatus);
        productRepository.save(product);
    }


}
