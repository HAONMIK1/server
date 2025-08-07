package kr.hhplus.be.server.product.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer price;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @CreationTimestamp
    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    @UpdateTimestamp
    @Column(name = "mdfcn_dt")
    private LocalDateTime mdfcnDt;

    public ProductEntity(String name, Integer price, Integer totalQuantity, Integer stockQuantity, ProductStatus status) {
        this.name = name;
        this.price = price;
        this.totalQuantity = totalQuantity;
        this.stockQuantity = stockQuantity;
        this.status = status;
    }

    public boolean canPurchase(int quantity) {
        return stockQuantity >= quantity && status == ProductStatus.AVAILABLE;
    }

    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new IllegalArgumentException("상품 재고가 부족합니다.");
        }
        this.stockQuantity -= quantity;
        if (this.stockQuantity == 0) {
            this.status = ProductStatus.SOLD_OUT;
        }
    }

    public enum ProductStatus {
        AVAILABLE, SOLD_OUT
    }
}