package kr.hhplus.be.server.coupon;

import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponRepository;
import kr.hhplus.be.server.coupon.application.service.CouponService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService 테스트")
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @InjectMocks
    private CouponService couponService;

    @Nested
    @DisplayName("쿠폰 발급 테스트")
    class IssueCouponTest {

        @Test
        void 쿠폰_발급_성공() {

        }

        @Test
        void 쿠폰_존재하지_않음_실패() {


        }

        @Test
        void 쿠폰_발급_불가능_상태_실패() {

        }

        @Test
        void 쿠폰_재고_부족_실패() {

        }

        @Test
        void 이미_발급받은_쿠폰_실패() {

        }
    }


}