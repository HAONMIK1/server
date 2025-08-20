package kr.hhplus.be.server.lock;

import kr.hhplus.be.server.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;


@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {
    
    private static final String REDISSON_LOCK_PREFIX = "LOCK:";
    
    private final RedissonClient redissonClient;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Around("@annotation(kr.hhplus.be.server.lock.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);
        
        // SpEL 표현식을 이용한 락 키 생성
        String lockKey = getLockKey(distributedLock.key(), method, joinPoint.getArgs(), signature.getParameterNames());
        RLock lock = redissonClient.getLock(REDISSON_LOCK_PREFIX + lockKey);
        
        log.info("DistributedLock AOP - Key: {}, Method: {}", lockKey, method.getName());
        
        try {
            // 락 획득 시도
            boolean isLocked = lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            
            if (!isLocked) {
                throw new RuntimeException("락을 획득할 수 없습니다. Key: " + lockKey);
            }
            
            log.info("락 획득 성공 - Key: {}", lockKey);
            
            // 비즈니스 로직 실행
            return joinPoint.proceed();
            
        } catch (InterruptedException e) {
            log.error("락 획득 중 인터럽트 발생 - Key: {}", lockKey, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("락 획득 중 인터럽트가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("락 처리 중 예외 발생 - Key: {}", lockKey, e);
            throw e;
        } finally {
            // 락 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("락 해제 완료 - Key: {}", lockKey);
            }
        }
    }
    
    /**
     * SpEL 표현식을 이용한 락 키 생성
     */
    private String getLockKey(String key, Method method, Object[] args, String[] parameterNames) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        // 메서드 파라미터를 SpEL 컨텍스트에 추가
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        
        // 메서드 정보를 SpEL 컨텍스트에 추가
        context.setVariable("methodName", method.getName());
        context.setVariable("className", method.getDeclaringClass().getSimpleName());
        
        try {
            return expressionParser.parseExpression(key).getValue(context, String.class);
        } catch (Exception e) {
            log.warn("SpEL 파싱 실패, 원본 키 사용: {}", key, e);
            return key;
        }
    }
}
