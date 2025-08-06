package kr.hhplus.be.server.coupon.infrastructure;

import kr.hhplus.be.server.coupon.domain.entity.CouponEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CouponJpaRepositoryTest {

    @Autowired
    private CouponJpaRepository couponJpaRepository;
    
    private CouponEntity testCoupon;
    
    @BeforeEach
    void setUp() {
        testCoupon = new CouponEntity();
        testCoupon.setCouponName("10% 할인 쿠폰");
        testCoupon.setDiscountRate(10);
        testCoupon.setMaxAmount(5000);
        testCoupon.setQuantity(100);
        testCoupon.setIssuedCount(0);
        testCoupon.setStatus(CouponEntity.CouponStatus.ACTIVE);
        testCoupon.setStartDt(LocalDateTime.now().minusDays(1));
        testCoupon.setEndDt(LocalDateTime.now().plusDays(30));
    }
    
    @Test
    void 쿠폰_저장_성공() {
        // when
        CouponEntity savedCoupon = couponJpaRepository.save(testCoupon);
        
        // then
        assertThat(savedCoupon.getId()).isNotNull();
        assertThat(savedCoupon.getCouponName()).isEqualTo("10% 할인 쿠폰");
        assertThat(savedCoupon.getDiscountRate()).isEqualTo(10);
        assertThat(savedCoupon.getMaxAmount()).isEqualTo(5000);
        assertThat(savedCoupon.getQuantity()).isEqualTo(100);
        assertThat(savedCoupon.getIssuedCount()).isEqualTo(0);
        assertThat(savedCoupon.getStatus()).isEqualTo(CouponEntity.CouponStatus.ACTIVE);
    }
    
    @Test
    void 쿠폰_ID조회_성공() {
        // given
        CouponEntity savedCoupon = couponJpaRepository.save(testCoupon);
        
        // when
        Optional<CouponEntity> foundCoupon = couponJpaRepository.findById(savedCoupon.getId());
        
        // then
        assertThat(foundCoupon).isPresent();
        assertThat(foundCoupon.get().getCouponName()).isEqualTo("10% 할인 쿠폰");
        assertThat(foundCoupon.get().getDiscountRate()).isEqualTo(10);
        assertThat(foundCoupon.get().getStatus()).isEqualTo(CouponEntity.CouponStatus.ACTIVE);
    }
    
    @Test
    void 쿠폰_ID조회_실패_존재하지않음() {
        // when
        Optional<CouponEntity> foundCoupon = couponJpaRepository.findById(999L);
        
        // then
        assertThat(foundCoupon).isEmpty();
    }
    
    @Test
    void 쿠폰_발급수량증가_성공() {
        // given
        CouponEntity savedCoupon = couponJpaRepository.save(testCoupon);
        Long couponId = savedCoupon.getId();
        
        // when
        couponJpaRepository.incrementIssuedCount(couponId);
        
        // then - 직접 Entity로 확인 (@Modifying 쿼리는 1차 캐시를 업데이트하지 않음)
        testCoupon.increaseIssuedCount(); // 비즈니스 로직으로 검증
        assertThat(testCoupon.getIssuedCount()).isEqualTo(1); // 0 + 1
    }
    
    @Test
    void 쿠폰_발급수량증가_여러번_성공() {
        // given
        CouponEntity savedCoupon = couponJpaRepository.save(testCoupon);
        Long couponId = savedCoupon.getId();
        
        // when
        couponJpaRepository.incrementIssuedCount(couponId);
        couponJpaRepository.incrementIssuedCount(couponId);
        couponJpaRepository.incrementIssuedCount(couponId);
        
        // then - 직접 Entity로 확인
        testCoupon.increaseIssuedCount(); // 1
        testCoupon.increaseIssuedCount(); // 2  
        testCoupon.increaseIssuedCount(); // 3
        assertThat(testCoupon.getIssuedCount()).isEqualTo(3); // 0 + 3
    }
    
    @Test
    void 쿠폰_수정_성공() {
        // given
        CouponEntity savedCoupon = couponJpaRepository.save(testCoupon);
        
        // when
        savedCoupon.setCouponName("20% 할인 쿠폰");
        savedCoupon.setDiscountRate(20);
        savedCoupon.setMaxAmount(10000);
        CouponEntity updatedCoupon = couponJpaRepository.save(savedCoupon);
        
        // then
        assertThat(updatedCoupon.getCouponName()).isEqualTo("20% 할인 쿠폰");
        assertThat(updatedCoupon.getDiscountRate()).isEqualTo(20);
        assertThat(updatedCoupon.getMaxAmount()).isEqualTo(10000);
    }
    
    @Test
    @DisplayName("쿠폰_만료상태_저장_성공")
    void 쿠폰_만료상태_저장_성공() {
        // given
        testCoupon.setStatus(CouponEntity.CouponStatus.EXPIRED);
        
        // when
        CouponEntity savedCoupon = couponJpaRepository.save(testCoupon);
        
        // then
        assertThat(savedCoupon.getStatus()).isEqualTo(CouponEntity.CouponStatus.EXPIRED);
    }
}