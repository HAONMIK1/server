package kr.hhplus.be.server.coupon;

import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponRepository;
import kr.hhplus.be.server.coupon.application.service.CouponService;
import kr.hhplus.be.server.coupon.domain.entity.CouponEntity;
import kr.hhplus.be.server.coupon.domain.entity.UserCouponEntity;
import kr.hhplus.be.server.coupon.presentation.dto.CouponResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService 단위 테스트")
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    @DisplayName("쿠폰 발급 성공")
    void 사용자_쿠폰ID_주어짐_쿠폰발급_수행_발급정보_반환() {
        // given
        Long userId = 1L;
        Long couponId = 1L;
        CouponEntity coupon = createValidCoupon();
        UserCouponEntity userCoupon = createUserCoupon(userId, couponId);

        given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));
        given(userCouponRepository.existsByUserIdAndCouponId(userId, couponId)).willReturn(false);
        given(userCouponRepository.save(any(UserCouponEntity.class))).willReturn(userCoupon);

        // when
        CouponResponse.Issue response = couponService.issueCoupon(userId, couponId);

        // then
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.couponId()).isEqualTo(couponId);
        verify(couponRepository).save(coupon);
        verify(userCouponRepository).save(any(UserCouponEntity.class));
    }

    @Test
    void 쿠폰_발급_성공() {
        // given
        Long userId = 1L;
        Long couponId = 1L;
        CouponEntity coupon = createValidCoupon();
        UserCouponEntity userCoupon = createUserCoupon(userId, couponId);

        given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));
        given(userCouponRepository.existsByUserIdAndCouponId(userId, couponId)).willReturn(false);
        given(userCouponRepository.save(any(UserCouponEntity.class))).willReturn(userCoupon);

        // when
        CouponResponse.Issue response = couponService.issueCoupon(userId, couponId);

        // then
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.couponId()).isEqualTo(couponId);
        verify(couponRepository).save(coupon);
        verify(userCouponRepository).save(any(UserCouponEntity.class));
    }

    @Test
    void 쿠폰_존재하지_않음_실패() {
        // given
        Long userId = 1L;
        Long couponId = 999L;
        given(couponRepository.findById(couponId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(userId, couponId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("쿠폰을 찾을 수 없습니다.");
    }

    @Test
    void 쿠폰_발급_불가능_상태_실패() {
        // given
        Long userId = 1L;
        Long couponId = 1L;
        CouponEntity coupon = createValidCoupon();
        coupon.setStatus(CouponEntity.CouponStatus.INACTIVE);

        given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(userId, couponId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("쿠폰을 발급할 수 없습니다. (품절 또는 기간 만료)");
    }

    @Test
    void 쿠폰_재고_부족_실패() {
        // given
        Long userId = 1L;
        Long couponId = 1L;
        CouponEntity coupon = createValidCoupon();
        coupon.setQuantity(10);
        coupon.setIssuedCount(10); // 재고와 발급수 동일 = 재고 부족

        given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(userId, couponId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("쿠폰을 발급할 수 없습니다. (품절 또는 기간 만료)");
    }

    @Test
    void 이미_발급받은_쿠폰_실패() {
        // given
        Long userId = 1L;
        Long couponId = 1L;
        CouponEntity coupon = createValidCoupon();

        given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));
        given(userCouponRepository.existsByUserIdAndCouponId(userId, couponId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(userId, couponId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 발급받은 쿠폰입니다.");
    }

    private CouponEntity createValidCoupon() {
        CouponEntity coupon = new CouponEntity();
        coupon.setId(1L);
        coupon.setCouponName("테스트 쿠폰");
        coupon.setDiscountRate(10);
        coupon.setMaxAmount(10000);
        coupon.setQuantity(100);
        coupon.setIssuedCount(0);
        coupon.setStatus(CouponEntity.CouponStatus.ACTIVE);
        coupon.setStartDt(LocalDateTime.now().minusDays(1));
        coupon.setEndDt(LocalDateTime.now().plusDays(7));
        return coupon;
    }

    private UserCouponEntity createUserCoupon(Long userId, Long couponId) {
        UserCouponEntity userCoupon = new UserCouponEntity();
        userCoupon.setId(1L);
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(UserCouponEntity.UserCouponStatus.UNUSED);
        userCoupon.setRegDt(LocalDateTime.now());
        return userCoupon;
    }
}