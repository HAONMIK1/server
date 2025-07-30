package kr.hhplus.be.server.product;

import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductEntityTest {

    @Test
    void 구매_가능_성공() {
        // given
        ProductEntity product = new ProductEntity();
        ReflectionTestUtils.setField(product, "stockQuantity", 10);
        ReflectionTestUtils.setField(product, "status", ProductEntity.ProductStatus.AVAILABLE);

        // when
        boolean result = product.canPurchase(5);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 구매_불가능_재고부족() {
        // given
        ProductEntity product = new ProductEntity();
        ReflectionTestUtils.setField(product, "stockQuantity", 3);
        ReflectionTestUtils.setField(product, "status", ProductEntity.ProductStatus.AVAILABLE);

        // when
        boolean result = product.canPurchase(5);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 구매_불가능_품절상태() {
        // given
        ProductEntity product = new ProductEntity();
        ReflectionTestUtils.setField(product, "stockQuantity", 10);
        ReflectionTestUtils.setField(product, "status", ProductEntity.ProductStatus.SOLD_OUT);

        // when
        boolean result = product.canPurchase(5);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 재고_차감_성공() {
        // given
        ProductEntity product = new ProductEntity();
        ReflectionTestUtils.setField(product, "stockQuantity", 10);
        ReflectionTestUtils.setField(product, "status", ProductEntity.ProductStatus.AVAILABLE);

        // when
        product.decreaseStock(3);

        // then
        assertThat(product.getStockQuantity()).isEqualTo(7);
        assertThat(product.getStatus()).isEqualTo(ProductEntity.ProductStatus.AVAILABLE);
    }

    @Test
    void 재고_차감_재고부족_예외() {
        // given
        ProductEntity product = new ProductEntity();
        ReflectionTestUtils.setField(product, "stockQuantity", 3);
        ReflectionTestUtils.setField(product, "status", ProductEntity.ProductStatus.AVAILABLE);

        // when & then
        assertThatThrownBy(() -> product.decreaseStock(5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 재고가 부족합니다.");
    }

    @Test
    void 재고_차감_품절상태_변경() {
        // given
        ProductEntity product = new ProductEntity();
        ReflectionTestUtils.setField(product, "stockQuantity", 3);
        ReflectionTestUtils.setField(product, "status", ProductEntity.ProductStatus.AVAILABLE);

        // when
        product.decreaseStock(3);

        // then
        assertThat(product.getStockQuantity()).isEqualTo(0);
        assertThat(product.getStatus()).isEqualTo(ProductEntity.ProductStatus.SOLD_OUT);
    }
}
