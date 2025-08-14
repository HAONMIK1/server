package kr.hhplus.be.server.balance;

import kr.hhplus.be.server.balance.application.service.BalanceService;
import kr.hhplus.be.server.balance.domain.entity.UserBalanceEntity;
import kr.hhplus.be.server.balance.domain.repository.UserBalanceRepository;
import kr.hhplus.be.server.balance.domain.entity.UserEntity;
import kr.hhplus.be.server.balance.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class BalanceConcurrencyTest {

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 잔액_차감_동시성() throws InterruptedException {
        // given
        // 사용자 및 초기 잔액 생성
        UserEntity testUser = userRepository.save(new UserEntity("테스트유저"));
        final Long userId = testUser.getId();
        final int initialBalance = 1000;
        userBalanceRepository.save(new UserBalanceEntity(userId, initialBalance));

        int threadCount = 2;
        int useAmount = 300;
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        // 2개의 스레드가 동시에 300원씩 차감 시도
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    System.out.println("Thread " + threadId + " 시작");
                    balanceService.use(userId, useAmount);
                    System.out.println("Thread " + threadId + " 성공");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("Thread " + threadId + " 실패: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        UserBalanceEntity finalBalance = balanceService.getBalance(userId);

        System.out.println("최종 잔액: " + finalBalance.getAmount());
        System.out.println("성공 카운트: " + successCount.get());
        System.out.println("실패 카운트: " + failCount.get());

        // Initial balance 1000 - (300 * 2) = final balance 400
        assertThat(finalBalance.getAmount()).isEqualTo(400);
        assertThat(successCount.get()).isEqualTo(2);
        assertThat(failCount.get()).isEqualTo(0);
    }
}
