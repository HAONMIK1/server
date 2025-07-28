package kr.hhplus.be.server.product.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "status", nullable = false)
    private ProductStatus status;

    @CreationTimestamp
    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    @UpdateTimestamp
    @Column(name = "mdfcn_dt")
    private LocalDateTime mdfcnDt;

    public boolean canPurchase(int quantity) {
        return stockQuantity >= quantity && status == ProductStatus.AVAILABLE;
    }

    public void decreaseStock(int quantity) {
        if (!canPurchase(quantity)) {
            throw new IllegalStateException("재고가 부족합니다.");
        }

        this.stockQuantity -= quantity;

        // 재고가 0이 되면 상태를 SOLD_OUT으로 변경
        if (this.stockQuantity == 0) {
            this.status = ProductStatus.SOLD_OUT;
        }
    }

    public void updateStatus(ProductStatus newStatus) {
        this.status = newStatus;
    }


    public enum ProductStatus {
        AVAILABLE, SOLD_OUT
    }

    public static ProductEntity createProduct(Long id, String name, Integer price, Integer stockQuantity) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setTotalQuantity(stockQuantity);
        product.setStockQuantity(stockQuantity);
        product.setStatus(ProductEntity.ProductStatus.AVAILABLE);
        return product;
    }
}