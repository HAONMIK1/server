package kr.hhplus.be.server.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    
    @Value("${spring.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.redis.port:6379}")
    private int redisPort;
    
    private static final String REDISSON_HOST_PREFIX = "redis://";
    
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(REDISSON_HOST_PREFIX + redisHost + ":" + redisPort)
                .setConnectionPoolSize(64)
                .setConnectionMinimumIdleSize(24)
                .setIdleConnectionTimeout(10000)
                .setConnectTimeout(10000)
                .setRetryAttempts(3)
                .setRetryInterval(1500)
                .setTimeout(3000);
        
        return Redisson.create(config);
    }
}
