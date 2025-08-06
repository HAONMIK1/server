package kr.hhplus.be.server.product.entity;

import kr.hhplus.be.server.product.domain.entity.ProductViewCountEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductViewCountEntityTest {

    @Test
    void 조회수_엔티티_생성_성공() {
        // given
        Long productId = 1L;
        int viewCount = 100;

        // when
        ProductViewCountEntity viewCountEntity = new ProductViewCountEntity();
        viewCountEntity.setProductId(productId);
        viewCountEntity.setViewCount(viewCount);

        // then
        assertThat(viewCountEntity.getProductId()).isEqualTo(productId);
        assertThat(viewCountEntity.getViewCount()).isEqualTo(viewCount);
    }

    @Test
    void 조회수_기본값_확인() {
        // when
        ProductViewCountEntity viewCountEntity = new ProductViewCountEntity();

        // then
        assertThat(viewCountEntity.getViewCount()).isEqualTo(0);
    }

    @Test
    void 조회수_증가_성공() {
        // given
        ProductViewCountEntity viewCountEntity = new ProductViewCountEntity();
        viewCountEntity.setProductId(1L);
        viewCountEntity.setViewCount(50);

        // when
        viewCountEntity.setViewCount(viewCountEntity.getViewCount() + 1);

        // then
        assertThat(viewCountEntity.getViewCount()).isEqualTo(51);
    }

    @Test
    void 조회수_대량증가_성공() {
        // given
        ProductViewCountEntity viewCountEntity = new ProductViewCountEntity();
        viewCountEntity.setProductId(1L);
        viewCountEntity.setViewCount(1000);

        // when
        viewCountEntity.setViewCount(viewCountEntity.getViewCount() + 100);

        // then
        assertThat(viewCountEntity.getViewCount()).isEqualTo(1100);
    }
} 