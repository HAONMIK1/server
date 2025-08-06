package kr.hhplus.be.server.coupon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.coupon.application.service.CouponService;
import kr.hhplus.be.server.coupon.presentation.controller.CouponController;
import kr.hhplus.be.server.coupon.presentation.dto.CouponResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CouponControllerTest {

    @InjectMocks
    private CouponController couponController;

    @Mock
    private CouponService couponService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(couponController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void 쿠폰_발급_성공() throws Exception {
        // given
        Long userId = 1L;
        Long couponId = 1L;
        CouponResponse.Issue response = new CouponResponse.Issue(
                1L,
                userId,
                couponId,
                "AVAILABLE",
                LocalDateTime.now()
        );
        given(couponService.issueCoupon(userId, couponId)).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/users/{userId}/coupons/{couponId}/issue", userId, couponId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userCouponId").value(1L))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.couponId").value(couponId))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andDo(print());

        verify(couponService).issueCoupon(userId, couponId);
    }
}