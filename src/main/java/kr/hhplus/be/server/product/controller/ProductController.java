package kr.hhplus.be.server.product.controller;

import kr.hhplus.be.server.product.dto.PopularProductResponse;
import kr.hhplus.be.server.product.dto.ProductResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController{
    // 상품 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse.Details> getDetails(@PathVariable Long id) {
        // Mock 데이터 생성
        ProductResponse.Details response = new ProductResponse.Details(
                id,
                "Sample",
                29900,
                100,
                75,
                "판매가능"
        );
        return ResponseEntity.ok(response);
    }
    @GetMapping("/popular")
    public ResponseEntity<List<PopularProductResponse.Details>> getPopularProducts(@PathVariable Long id) {
        // Mock 데이터 생성
        return ResponseEntity.ok(List.of(
                new PopularProductResponse.Details(1L, "상품A", 500, 1000, LocalDateTime.parse("2025-06-01T12:00:00")),
                new PopularProductResponse.Details(2L, "상품B", 400, 800, LocalDateTime.parse("2025-07-01T12:00:00"))
        ));
    }

}