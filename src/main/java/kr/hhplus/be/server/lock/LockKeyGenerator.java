package kr.hhplus.be.server.lock;

import java.lang.reflect.Method;

public interface LockKeyGenerator {
    String generateKey(String keyExpression, Method method, Object[] args, String[] parameterNames);
}
