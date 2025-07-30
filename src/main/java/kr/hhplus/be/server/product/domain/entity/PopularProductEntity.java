package kr.hhplus.be.server.product.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "popular_product")
@Getter
@NoArgsConstructor
public class PopularProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "sales_count", nullable = false)
    private Integer salesCount;

    @CreationTimestamp
    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private ProductEntity product;

    public static PopularProductEntity createPopularProduct(Long productId, int viewCount, int salesCount) {
        PopularProductEntity popularProduct = new PopularProductEntity();
        popularProduct.productId = productId;
        popularProduct.viewCount = viewCount;
        popularProduct.salesCount = salesCount;
        return popularProduct;
    }
}