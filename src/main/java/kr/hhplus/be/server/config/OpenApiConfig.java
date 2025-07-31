package kr.hhplus.be.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Step 2
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("이커머스 API")
                        .version("v1.0")
                        .description("잔액, 상품, 쿠폰, 주문 관리 API"));
    }
}