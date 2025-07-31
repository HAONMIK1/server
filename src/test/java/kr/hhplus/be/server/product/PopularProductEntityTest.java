package kr.hhplus.be.server.product;

import kr.hhplus.be.server.product.domain.entity.PopularProductEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PopularProductEntityTest {

    @Test
    void 인기상품_생성_성공() {
        // given
        Long productId = 1L;
        int viewCount = 100;
        int salesCount = 50;

        // when
        PopularProductEntity popularProduct = PopularProductEntity.createPopularProduct(productId, viewCount, salesCount);

        // then
        assertThat(popularProduct.getProductId()).isEqualTo(productId);
        assertThat(popularProduct.getViewCount()).isEqualTo(viewCount);
        assertThat(popularProduct.getSalesCount()).isEqualTo(salesCount);
    }

} 