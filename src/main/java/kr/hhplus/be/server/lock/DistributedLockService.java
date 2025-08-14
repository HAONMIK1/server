package kr.hhplus.be.server.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class DistributedLockService {
    
    private static final String LOCK_PREFIX = "LOCK:";
    
    private final RedissonClient redissonClient;
    
    /**
     * 락 획득 시도
     * 
     * @param lockKey 락 키
     * @param waitTime 대기 시간
     * @param leaseTime 임대 시간
     * @param timeUnit 시간 단위
     * @return 락 획득 성공 여부
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        
        try {
            boolean isLocked = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (isLocked) {
                log.info("락 획득 성공 - Key: {}", lockKey);
            } else {
                log.warn("락 획득 실패 - Key: {}", lockKey);
            }
            return isLocked;
        } catch (InterruptedException e) {
            log.error("락 획득 중 인터럽트 발생 - Key: {}", lockKey, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    /**
     * 락 해제
     * 
     * @param lockKey 락 키
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.info("락 해제 완료 - Key: {}", lockKey);
        } else {
            log.warn("현재 스레드가 보유하지 않은 락을 해제하려고 시도 - Key: {}", lockKey);
        }
    }
    
    /**
     * 락 상태 확인
     * 
     * @param lockKey 락 키
     * @return 락이 잠겨있는지 여부
     */
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        return lock.isLocked();
    }
    
    /**
     * 현재 스레드가 락을 보유하고 있는지 확인
     * 
     * @param lockKey 락 키
     * @return 현재 스레드가 락을 보유하고 있는지 여부
     */
    public boolean isHeldByCurrentThread(String lockKey) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        return lock.isHeldByCurrentThread();
    }
    
    /**
     * 락 정보 조회
     * 
     * @param lockKey 락 키
     * @return 락 정보
     */
    public LockInfo getLockInfo(String lockKey) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        
        return LockInfo.builder()
                .key(lockKey)
                .isLocked(lock.isLocked())
                .isHeldByCurrentThread(lock.isHeldByCurrentThread())
                .holdCount(lock.getHoldCount())
                .remainTimeToLive(lock.remainTimeToLive())
                .build();
    }
    
    /**
     * 락 정보를 담는 내부 클래스
     */
    public static class LockInfo {
        private final String key;
        private final boolean isLocked;
        private final boolean isHeldByCurrentThread;
        private final int holdCount;
        private final long remainTimeToLive;
        
        private LockInfo(Builder builder) {
            this.key = builder.key;
            this.isLocked = builder.isLocked;
            this.isHeldByCurrentThread = builder.isHeldByCurrentThread;
            this.holdCount = builder.holdCount;
            this.remainTimeToLive = builder.remainTimeToLive;
        }
        
        // Getters
        public String getKey() { return key; }
        public boolean isLocked() { return isLocked; }
        public boolean isHeldByCurrentThread() { return isHeldByCurrentThread; }
        public int getHoldCount() { return holdCount; }
        public long getRemainTimeToLive() { return remainTimeToLive; }
        
        // Builder
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String key;
            private boolean isLocked;
            private boolean isHeldByCurrentThread;
            private int holdCount;
            private long remainTimeToLive;
            
            public Builder key(String key) {
                this.key = key;
                return this;
            }
            
            public Builder isLocked(boolean isLocked) {
                this.isLocked = isLocked;
                return this;
            }
            
            public Builder isHeldByCurrentThread(boolean isHeldByCurrentThread) {
                this.isHeldByCurrentThread = isHeldByCurrentThread;
                return this;
            }
            
            public Builder holdCount(int holdCount) {
                this.holdCount = holdCount;
                return this;
            }
            
            public Builder remainTimeToLive(long remainTimeToLive) {
                this.remainTimeToLive = remainTimeToLive;
                return this;
            }
            
            public LockInfo build() {
                return new LockInfo(this);
            }
        }
    }
}
