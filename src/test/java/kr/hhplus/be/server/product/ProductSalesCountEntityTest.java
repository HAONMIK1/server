package kr.hhplus.be.server.product;

import kr.hhplus.be.server.product.domain.entity.ProductSalesCountEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductSalesCountEntityTest {

    @Test
    void 판매수_엔티티_생성_성공() {
        // given
        Long productId = 1L;
        int salesCount = 50;

        // when
        ProductSalesCountEntity salesCountEntity = new ProductSalesCountEntity();
        salesCountEntity.setProductId(productId);
        salesCountEntity.setSalesCount(salesCount);

        // then
        assertThat(salesCountEntity.getProductId()).isEqualTo(productId);
        assertThat(salesCountEntity.getSalesCount()).isEqualTo(salesCount);
    }

    @Test
    void 판매수_기본값_확인() {
        // when
        ProductSalesCountEntity salesCountEntity = new ProductSalesCountEntity();

        // then
        assertThat(salesCountEntity.getSalesCount()).isEqualTo(0);
    }

    @Test
    void 판매수_증가_성공() {
        // given
        ProductSalesCountEntity salesCountEntity = new ProductSalesCountEntity();
        salesCountEntity.setProductId(1L);
        salesCountEntity.setSalesCount(25);

        // when
        salesCountEntity.setSalesCount(salesCountEntity.getSalesCount() + 1);

        // then
        assertThat(salesCountEntity.getSalesCount()).isEqualTo(26);
    }

} 