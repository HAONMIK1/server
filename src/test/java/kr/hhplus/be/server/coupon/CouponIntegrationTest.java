package kr.hhplus.be.server.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.balance.domain.entity.UserEntity;
import kr.hhplus.be.server.balance.domain.repository.UserRepository;
import kr.hhplus.be.server.coupon.domain.entity.CouponEntity;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
public class CouponIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    private UserEntity testUser;
    private CouponEntity testCoupon;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = new UserEntity();
        testUser.setUserName("테스트 사용자");
        testUser = userRepository.save(testUser);

        // 테스트용 쿠폰 생성 (실제 필드명 사용)
        testCoupon = new CouponEntity();
        ReflectionTestUtils.setField(testCoupon, "couponName", "테스트 쿠폰");
        ReflectionTestUtils.setField(testCoupon, "discountRate", 10); // 10% 할인
        ReflectionTestUtils.setField(testCoupon, "maxAmount", 5000);
        ReflectionTestUtils.setField(testCoupon, "quantity", 100);
        ReflectionTestUtils.setField(testCoupon, "issuedCount", 0);
        ReflectionTestUtils.setField(testCoupon, "status", CouponEntity.CouponStatus.ACTIVE);
        ReflectionTestUtils.setField(testCoupon, "startDt", LocalDateTime.now().minusDays(1));
        ReflectionTestUtils.setField(testCoupon, "endDt", LocalDateTime.now().plusDays(30));
        testCoupon = couponRepository.save(testCoupon);
    }

    @Test
    void 쿠폰_발급_API_호출_테스트() throws Exception {
        // when & then - 단순히 API 호출이 되는지만 확인
        mockMvc.perform(post("/api/v1/users/{userId}/coupons/{couponId}/issue", 
                        testUser.getId(), testCoupon.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userCouponId").exists())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void 사용자_쿠폰_조회_API_호출_테스트() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/users/{userId}/coupons", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}