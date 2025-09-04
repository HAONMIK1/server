package kr.hhplus.be.server.payment.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentMockDataPlatformClient {
    public void send(Long paymentId) {
        log.info("[MockDataPlatform] payment sent: paymentId={}", paymentId);
    }
}


