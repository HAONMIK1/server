package kr.hhplus.be.server.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@Slf4j
public class SpelLockKeyGenerator implements LockKeyGenerator {
    
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    
    @Override
    public String generateKey(String keyExpression, Method method, Object[] args, String[] parameterNames) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        
        context.setVariable("methodName", method.getName());
        context.setVariable("className", method.getDeclaringClass().getSimpleName());
        
        try {
            return expressionParser.parseExpression(keyExpression).getValue(context, String.class);
        } catch (Exception e) {
            log.warn("SpEL 파싱 실패, 원본 키 사용: {}", keyExpression, e);
            return keyExpression;
        }
    }
}
