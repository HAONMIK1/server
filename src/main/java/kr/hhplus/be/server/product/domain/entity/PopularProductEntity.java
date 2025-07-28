package kr.hhplus.be.server.product.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "popular_product")
@Getter
@Setter
@NoArgsConstructor
public class PopularProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
        popularProduct.setViewCount(viewCount);
        popularProduct.setSalesCount(salesCount);
        return popularProduct;
    }
}