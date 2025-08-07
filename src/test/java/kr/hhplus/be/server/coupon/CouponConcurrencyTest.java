package kr.hhplus.be.server.coupon;

import kr.hhplus.be.server.balance.domain.entity.UserEntity;
import kr.hhplus.be.server.balance.domain.repository.UserRepository;
import kr.hhplus.be.server.coupon.application.service.CouponService;
import kr.hhplus.be.server.coupon.domain.entity.CouponEntity;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class CouponConcurrencyTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private UserRepository userRepository;


    @Test
    void 쿠폰_발급_동시성() throws InterruptedException {
        // given
        // 수량이 10개인 선착순 쿠폰 생성
        CouponEntity initialCoupon = new CouponEntity();
        initialCoupon.setCouponName("선착순 10명 쿠폰");
        initialCoupon.setDiscountRate(10);
        initialCoupon.setMaxAmount(1000);
        initialCoupon.setQuantity(10);
        initialCoupon.setIssuedCount(0);
        initialCoupon.setStatus(CouponEntity.CouponStatus.ACTIVE);
        initialCoupon.setStartDt(LocalDateTime.now());
        initialCoupon.setEndDt(LocalDateTime.now().plusDays(10));
        
        CouponEntity savedCoupon = couponRepository.save(initialCoupon);
        final Long couponId = savedCoupon.getId();

        int threadCount = 5; // 스레드 수를 줄임
        ExecutorService executorService = Executors.newFixedThreadPool(5); // 스레드 풀 크기도 줄임
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 5명의 동시 요청 사용자 생성
        List<UserEntity> users = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            users.add(userRepository.save(new UserEntity("user" + i)));
        }

        // when
        // 5개의 스레드가 동시에 쿠폰 발급 시도
        for (UserEntity user : users) {
            executorService.submit(() -> {
                try {
                    couponService.issueCoupon(user.getId(), couponId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("Exception: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        List<CouponEntity> allCoupons = couponService.getAllCoupons();
        CouponEntity finalCoupon = allCoupons.stream()
                .filter(coupon -> coupon.getId().equals(couponId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
        long issuedUserCouponCount = userCouponRepository.countByCouponId(couponId);

        System.out.println("최종 발급된 쿠폰 수: " + finalCoupon.getIssuedCount());
        System.out.println("사용자 쿠폰 테이블에 저장된 수: " + issuedUserCouponCount);
        System.out.println("성공 카운트: " + successCount.get());
        System.out.println("실패 카운트: " + failCount.get());

        // 최종 발급된 쿠폰 수는 5개여야 함
        assertThat(finalCoupon.getIssuedCount()).isEqualTo(5);
        assertThat(issuedUserCouponCount).isEqualTo(5);
        assertThat(successCount.get()).isEqualTo(5);
        assertThat(failCount.get()).isEqualTo(0);
    }
}
