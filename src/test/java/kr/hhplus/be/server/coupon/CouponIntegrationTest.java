package kr.hhplus.be.server.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.coupon.application.service.CouponService;
import kr.hhplus.be.server.coupon.domain.entity.CouponEntity;
import kr.hhplus.be.server.coupon.domain.entity.UserCouponEntity;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
@DisplayName("Coupon 도메인 통합 테스트")
public class CouponIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    private CouponEntity testCoupon1;
    private CouponEntity testCoupon2;
    private CouponEntity expiredCoupon;

    @BeforeEach
    void setUp() {
        // 테스트용 활성 쿠폰 1
        testCoupon1 = new CouponEntity();
        testCoupon1.setCouponName("10% 할인 쿠폰");
        testCoupon1.setDiscountRate(10);
        testCoupon1.setMaxAmount(5000);
        testCoupon1.setQuantity(100);
        testCoupon1.setIssuedCount(0);
        testCoupon1.setStatus(CouponEntity.CouponStatus.ACTIVE);
        testCoupon1.setStartDt(LocalDateTime.now().minusDays(1));
        testCoupon1.setEndDt(LocalDateTime.now().plusDays(30));
        testCoupon1 = couponRepository.save(testCoupon1);

        // 테스트용 활성 쿠폰 2
        testCoupon2 = new CouponEntity();
        testCoupon2.setCouponName("15% 할인 쿠폰");
        testCoupon2.setDiscountRate(15);
        testCoupon2.setMaxAmount(3000);
        testCoupon2.setQuantity(50);
        testCoupon2.setIssuedCount(0);
        testCoupon2.setStatus(CouponEntity.CouponStatus.ACTIVE);
        testCoupon2.setStartDt(LocalDateTime.now().minusDays(1));
        testCoupon2.setEndDt(LocalDateTime.now().plusDays(30));
        testCoupon2 = couponRepository.save(testCoupon2);

        // 테스트용 만료 쿠폰
        expiredCoupon = new CouponEntity();
        expiredCoupon.setCouponName("만료된 쿠폰");
        expiredCoupon.setDiscountRate(20);
        expiredCoupon.setMaxAmount(10000);
        expiredCoupon.setQuantity(10);
        expiredCoupon.setIssuedCount(0);
        expiredCoupon.setStatus(CouponEntity.CouponStatus.EXPIRED);
        expiredCoupon.setStartDt(LocalDateTime.now().minusDays(10));
        expiredCoupon.setEndDt(LocalDateTime.now().minusDays(1));
        expiredCoupon = couponRepository.save(expiredCoupon);
    }

    @Test
    @DisplayName("쿠폰_전체조회_API_성공")
    void 쿠폰_전체조회_API_성공() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/coupons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3)) // 활성 2개 + 만료 1개
                .andExpect(jsonPath("$[0].couponName").exists())
                .andExpect(jsonPath("$[0].discountRate").exists())
                .andExpect(jsonPath("$[0].status").exists());
    }

    @Test
    @DisplayName("쿠폰_발급_API_성공")
    void 쿠폰_발급_API_성공() throws Exception {
        // given
        Long userId = 1L;
        Long couponId = testCoupon1.getId();

        // when & then
        mockMvc.perform(post("/api/v1/users/{userId}/coupons/{couponId}/issue", userId, couponId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userCouponId").exists())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.couponId").value(testCoupon1.getId()))
                .andExpect(jsonPath("$.status").value("UNUSED"))
                .andExpect(jsonPath("$.issuedDt").exists());
    }

    @Test
    @DisplayName("쿠폰_발급_API_실패_만료된쿠폰")
    void 쿠폰_발급_API_실패_만료된쿠폰() throws Exception {
        // given
        Long userId = 1L;
        Long couponId = expiredCoupon.getId();

        // when & then - 예외가 발생하는 것을 확인
        mockMvc.perform(post("/api/v1/users/{userId}/coupons/{couponId}/issue", userId, couponId))
                .andExpect(result -> {
                    // 예외가 발생했는지만 확인
                    assertThat(result.getResolvedException()).isNotNull();
                });
    }

    @Test
    @DisplayName("쿠폰_발급_API_실패_존재하지않는쿠폰")
    void 쿠폰_발급_API_실패_존재하지않는쿠폰() throws Exception {
        // given
        Long userId = 1L;
        Long couponId = 999L;

        // when & then - 예외가 발생하는 것을 확인
        mockMvc.perform(post("/api/v1/users/{userId}/coupons/{couponId}/issue", userId, couponId))
                .andExpect(result -> {
                    // 예외가 발생했는지만 확인
                    assertThat(result.getResolvedException()).isNotNull();
                });
    }

    @Test
    @DisplayName("사용자쿠폰_조회_API_성공")
    void 사용자쿠폰_조회_API_성공() throws Exception {
        // given
        Long userId = 1L;
        UserCouponEntity userCoupon1 = UserCouponEntity.create(userId, testCoupon1.getId());
        UserCouponEntity userCoupon2 = UserCouponEntity.create(userId, testCoupon2.getId());
        userCouponRepository.save(userCoupon1);
        userCouponRepository.save(userCoupon2);

        // when & then
        mockMvc.perform(get("/api/v1/users/{userId}/coupons", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[0].status").value("UNUSED"));
    }

    @Test
    @DisplayName("사용자쿠폰_조회_API_성공_빈목록")
    void 사용자쿠폰_조회_API_성공_빈목록() throws Exception {
        // given
        Long userId = 999L; // 쿠폰이 없는 사용자

        // when & then
        mockMvc.perform(get("/api/v1/users/{userId}/coupons", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("쿠폰_발급_서비스계층_성공")
    void 쿠폰_발급_서비스계층_성공() {
        // given
        Long userId = 1L;
        Long couponId = testCoupon1.getId();

        // when
        var response = couponService.issueCoupon(userId, couponId);

        // then
        assertThat(response.userCouponId()).isNotNull();
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.couponId()).isEqualTo(couponId);
        assertThat(response.issuedDt()).isNotNull();

        // DB 확인
        UserCouponEntity savedUserCoupon = userCouponRepository.findById(response.userCouponId()).orElse(null);
        assertThat(savedUserCoupon).isNotNull();
        assertThat(savedUserCoupon.getStatus()).isEqualTo(UserCouponEntity.UserCouponStatus.UNUSED);

        // 쿠폰 발급 수량 증가 확인
        CouponEntity updatedCoupon = couponRepository.findById(couponId).orElse(null);
        assertThat(updatedCoupon).isNotNull();
        assertThat(updatedCoupon.getIssuedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("쿠폰_발급_서비스계층_실패_만료된쿠폰")
    void 쿠폰_발급_서비스계층_실패_만료된쿠폰() {
        // given
        Long userId = 1L;
        Long couponId = expiredCoupon.getId();

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(userId, couponId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("발급할 수 없는 쿠폰입니다.");
    }

    @Test
    @DisplayName("쿠폰_발급_서비스계층_실패_중복발급")
    void 쿠폰_발급_서비스계층_실패_중복발급() {
        // given
        Long userId = 1L;
        Long couponId = testCoupon1.getId();

        // 첫 번째 발급
        couponService.issueCoupon(userId, couponId);

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(userId, couponId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 발급받은 쿠폰입니다.");
    }

    @Test
    @DisplayName("쿠폰_사용_서비스계층_성공")
    void 쿠폰_사용_서비스계층_성공() {
        // given
        Long userId = 1L;
        Long couponId = testCoupon1.getId();

        // 쿠폰 발급
        UserCouponEntity userCoupon = UserCouponEntity.create(userId, couponId);
        userCoupon = userCouponRepository.save(userCoupon);

        // when
        couponService.useCoupon(userId);

        // then
        UserCouponEntity usedCoupon = userCouponRepository.findById(userCoupon.getId()).orElse(null);
        assertThat(usedCoupon).isNotNull();
        assertThat(usedCoupon.getStatus()).isEqualTo(UserCouponEntity.UserCouponStatus.USED);
    }

    @Test
    @DisplayName("쿠폰_사용_서비스계층_실패_발급받지않은쿠폰")
    void 쿠폰_사용_서비스계층_실패_발급받지않은쿠폰() {
        // given
        Long userId = 1L;

        // when & then
        assertThatThrownBy(() -> couponService.useCoupon(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("발급받지 않은 쿠폰입니다.");
    }

    @Test
    @DisplayName("사용자쿠폰_조회_서비스계층_성공")
    void 사용자쿠폰_조회_서비스계층_성공() {
        // given
        Long userId = 1L;
        UserCouponEntity userCoupon1 = UserCouponEntity.create(userId, testCoupon1.getId());
        UserCouponEntity userCoupon2 = UserCouponEntity.create(userId, testCoupon2.getId());
        userCouponRepository.save(userCoupon1);
        userCouponRepository.save(userCoupon2);

        // when
        List<UserCouponEntity> userCoupons = couponService.getUserCoupons(userId);

        // then
        assertThat(userCoupons).hasSize(2);
        assertThat(userCoupons).allMatch(coupon -> coupon.getUserId().equals(userId));
        assertThat(userCoupons).allMatch(coupon -> coupon.getStatus().equals(UserCouponEntity.UserCouponStatus.UNUSED));
    }
}