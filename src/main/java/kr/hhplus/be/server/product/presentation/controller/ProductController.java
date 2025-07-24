package kr.hhplus.be.server.product.presentation.controller;

import kr.hhplus.be.server.product.application.service.ProductService;
import kr.hhplus.be.server.product.domain.entity.PopularProductEntity;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    // 상품 목록 조회
    @GetMapping
    public ResponseEntity<List<ProductEntity>> getProducts() {
        List<ProductEntity> products = productService.getProducts();
        return ResponseEntity.ok(products);
    }
    // 상품 상세 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ProductEntity> getProduct(@PathVariable("productId") Long productId) {
        ProductEntity products = productService.getProduct(productId);
        return ResponseEntity.ok(products);
    }



}