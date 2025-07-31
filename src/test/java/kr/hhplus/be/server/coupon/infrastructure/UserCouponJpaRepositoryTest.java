package kr.hhplus.be.server.coupon.infrastructure;

import kr.hhplus.be.server.coupon.domain.entity.CouponEntity;
import kr.hhplus.be.server.coupon.domain.entity.UserCouponEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserCouponJpaRepository 테스트")
class UserCouponJpaRepositoryTest {

    @Autowired
    private UserCouponJpaRepository userCouponJpaRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private UserCouponEntity testUserCoupon;
    private CouponEntity testCoupon;
    
    @BeforeEach
    void setUp() {
        // 테스트용 쿠폰 생성
        testCoupon = new CouponEntity();
        testCoupon.setCouponName("테스트 쿠폰");
        testCoupon.setDiscountRate(10);
        testCoupon.setMaxAmount(5000);
        testCoupon.setQuantity(100);
        testCoupon.setIssuedCount(0);
        testCoupon.setStatus(CouponEntity.CouponStatus.ACTIVE);
        testCoupon.setStartDt(LocalDateTime.now().minusDays(1));
        testCoupon.setEndDt(LocalDateTime.now().plusDays(30));
        testCoupon = entityManager.persistAndFlush(testCoupon);
        
        // 테스트용 사용자 쿠폰 생성
        testUserCoupon = UserCouponEntity.create(1L, testCoupon.getId());
        
        entityManager.clear();
    }
    
    @Test
    @DisplayName("사용자쿠폰_저장_성공")
    void 사용자쿠폰_저장_성공() {
        // when
        UserCouponEntity savedUserCoupon = userCouponJpaRepository.save(testUserCoupon);
        
        // then
        assertThat(savedUserCoupon.getId()).isNotNull();
        assertThat(savedUserCoupon.getUserId()).isEqualTo(1L);
        assertThat(savedUserCoupon.getCouponId()).isEqualTo(testCoupon.getId());
        assertThat(savedUserCoupon.getStatus()).isEqualTo(UserCouponEntity.UserCouponStatus.UNUSED);
    }
    
    @Test
    @DisplayName("사용자쿠폰_ID조회_성공")
    void 사용자쿠폰_ID조회_성공() {
        // given
        UserCouponEntity savedUserCoupon = userCouponJpaRepository.save(testUserCoupon);
        
        // when
        Optional<UserCouponEntity> foundUserCoupon = userCouponJpaRepository.findById(savedUserCoupon.getId());
        
        // then
        assertThat(foundUserCoupon).isPresent();
        assertThat(foundUserCoupon.get().getUserId()).isEqualTo(1L);
        assertThat(foundUserCoupon.get().getCouponId()).isEqualTo(testCoupon.getId());
        assertThat(foundUserCoupon.get().getStatus()).isEqualTo(UserCouponEntity.UserCouponStatus.UNUSED);
    }
    
    @Test
    @DisplayName("사용자쿠폰_ID조회_실패_존재하지않음")
    void 사용자쿠폰_ID조회_실패_존재하지않음() {
        // when
        Optional<UserCouponEntity> foundUserCoupon = userCouponJpaRepository.findById(999L);
        
        // then
        assertThat(foundUserCoupon).isEmpty();
    }
    
    @Test
    @DisplayName("사용자쿠폰_사용자ID_쿠폰ID조회_성공")
    void 사용자쿠폰_사용자ID_쿠폰ID조회_성공() {
        // given
        userCouponJpaRepository.save(testUserCoupon);
        
        // when
        Optional<UserCouponEntity> foundUserCoupon = userCouponJpaRepository.findByUserIdAndCouponId(1L, testCoupon.getId());
        
        // then
        assertThat(foundUserCoupon).isPresent();
        assertThat(foundUserCoupon.get().getUserId()).isEqualTo(1L);
        assertThat(foundUserCoupon.get().getCouponId()).isEqualTo(testCoupon.getId());
    }
    
    @Test
    @DisplayName("사용자쿠폰_사용자ID_쿠폰ID조회_실패_존재하지않음")
    void 사용자쿠폰_사용자ID_쿠폰ID조회_실패_존재하지않음() {
        // when
        Optional<UserCouponEntity> foundUserCoupon = userCouponJpaRepository.findByUserIdAndCouponId(999L, testCoupon.getId());
        
        // then
        assertThat(foundUserCoupon).isEmpty();
    }
    
    @Test
    @DisplayName("사용자쿠폰_사용처리_성공")
    void 사용자쿠폰_사용처리_성공() {
        // given
        UserCouponEntity savedUserCoupon = userCouponJpaRepository.save(testUserCoupon);
        
        // when
        savedUserCoupon.use();
        UserCouponEntity updatedUserCoupon = userCouponJpaRepository.save(savedUserCoupon);
        
        // then
        assertThat(updatedUserCoupon.getStatus()).isEqualTo(UserCouponEntity.UserCouponStatus.USED);
    }
    
    @Test
    @DisplayName("사용자쿠폰_다중사용자_저장_성공")
    void 사용자쿠폰_다중사용자_저장_성공() {
        // given
        UserCouponEntity userCoupon1 = UserCouponEntity.create(1L, testCoupon.getId());
        UserCouponEntity userCoupon2 = UserCouponEntity.create(2L, testCoupon.getId());
        UserCouponEntity userCoupon3 = UserCouponEntity.create(3L, testCoupon.getId());
        
        // when
        UserCouponEntity savedUserCoupon1 = userCouponJpaRepository.save(userCoupon1);
        UserCouponEntity savedUserCoupon2 = userCouponJpaRepository.save(userCoupon2);
        UserCouponEntity savedUserCoupon3 = userCouponJpaRepository.save(userCoupon3);
        
        // then
        assertThat(savedUserCoupon1.getUserId()).isEqualTo(1L);
        assertThat(savedUserCoupon2.getUserId()).isEqualTo(2L);
        assertThat(savedUserCoupon3.getUserId()).isEqualTo(3L);
        
        // 각 사용자별 쿠폰 존재 확인
        assertThat(userCouponJpaRepository.findByUserIdAndCouponId(1L, testCoupon.getId())).isPresent();
        assertThat(userCouponJpaRepository.findByUserIdAndCouponId(2L, testCoupon.getId())).isPresent();
        assertThat(userCouponJpaRepository.findByUserIdAndCouponId(3L, testCoupon.getId())).isPresent();
        assertThat(userCouponJpaRepository.findByUserIdAndCouponId(4L, testCoupon.getId())).isEmpty();
    }
}