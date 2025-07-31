package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.entity.PopularProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PopularProductJpaRepository extends JpaRepository<PopularProductEntity, Long> {

    // 상품 메인 테이블과 판매량, 조회수 테이블 JOIN하여 상위 5개 인기상품 조회
    @Query(value = "SELECT p.id, " +
            "COALESCE(ps.sales_count, 0) as sales_count, " +
            "COALESCE(pv.view_count, 0) as view_count, " +
            "p.reg_dt " +
            "FROM product p " +
            "INNER JOIN product_sales_count ps ON p.id = ps.product_id " +
            "INNER JOIN product_view_count pv ON p.id = pv.product_id " +
            "ORDER BY ps.sales_count DESC, pv.view_count DESC, p.reg_dt DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Object[]> findPopularProducts();

    // 인기상품 테이블에서 모든 데이터 조회
    @Query("SELECT pp FROM PopularProductEntity pp ORDER BY pp.viewCount DESC, pp.salesCount DESC")
    List<PopularProductEntity> findPopularProductsOrderedByPriority();

    // 인기상품 테이블 초기화
    @Modifying
    @Query("DELETE FROM PopularProductEntity pp")
    void deleteAllPopularProducts();
} 