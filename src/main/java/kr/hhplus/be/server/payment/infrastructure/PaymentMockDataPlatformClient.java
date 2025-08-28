package kr.hhplus.be.server.payment.infrastructure;

import org.springframework.stereotype.Component;

@Component
public class PaymentMockDataPlatformClient {
    public void send(Long paymentId) {
        System.out.println("[MockDataPlatform] payment sent: paymentId=" + paymentId);
    }
}


