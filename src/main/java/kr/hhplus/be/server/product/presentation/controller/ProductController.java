package kr.hhplus.be.server.product.presentation.controller;

import kr.hhplus.be.server.product.application.service.ProductService;
import kr.hhplus.be.server.product.domain.entity.PopularProductEntity;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import kr.hhplus.be.server.product.domain.entity.ProductSalesCountEntity;
import kr.hhplus.be.server.product.presentation.dto.PopularProductResponse;
import kr.hhplus.be.server.product.presentation.dto.ProductResponse;
import kr.hhplus.be.server.product.presentation.dto.SalesRankingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    // 상품 목록 조회
    @GetMapping
    public ResponseEntity<List<ProductResponse.Details>> getProducts() {
        List<ProductEntity> products = productService.getProducts();
        List<ProductResponse.Details> response = products.stream()
                .map(ProductResponse.Details::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // 상품 상세 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse.Details> getProduct(@PathVariable("productId") Long productId) {
        ProductEntity product = productService.getProduct(productId);
        ProductResponse.Details response = ProductResponse.Details.from(product);
        return ResponseEntity.ok(response);
    }

    //인기상품 조회
    @GetMapping("/popular")
    public ResponseEntity<List<PopularProductResponse.Details>> getPopularProducts() {
        List<PopularProductEntity> products = productService.getPopularProducts();
        List<PopularProductResponse.Details> response = products.stream()
                .map(PopularProductResponse.Details::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    //인기상품 조회
    @GetMapping("/popular/ranking")
    public ResponseEntity<List<SalesRankingResponse.Item>> getRankingProducts() {
        List<ProductSalesCountEntity> products = productService.getRankingProducts();
        List<SalesRankingResponse.Item> response = products.stream()
                .map(p -> new SalesRankingResponse.Item(p.getProductId(), p.getSalesCount()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    //인기상품 배치
    @PostMapping("/popular/update")
    public ResponseEntity<String> updatePopularProducts() {
        productService.updatePopularProducts();
        return ResponseEntity.ok("인기상품 테이블이 업데이트되었습니다.");
    }
}