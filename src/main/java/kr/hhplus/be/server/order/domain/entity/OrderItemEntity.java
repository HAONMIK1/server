package kr.hhplus.be.server.order.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false)
    private Integer price;

    @CreationTimestamp
    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    public static OrderItemEntity createOrderItem(ProductEntity product, int quantity) {
        return OrderItemEntity.builder()
                .productId(product.getId())
                .quantity(quantity)
                .price(product.getPrice() * quantity)
                .build();
    }
}