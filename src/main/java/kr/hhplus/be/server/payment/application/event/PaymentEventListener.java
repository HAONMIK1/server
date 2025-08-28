package kr.hhplus.be.server.payment.application.event;

import kr.hhplus.be.server.payment.infrastructure.PaymentMockDataPlatformClient;
import kr.hhplus.be.server.payment.domain.entity.PaymentEntity;
import kr.hhplus.be.server.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentMockDataPlatformClient client;
    private final PaymentRepository paymentRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(Long paymentId) {
        try {
            PaymentEntity payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment == null) return;
            client.send(payment.getId());
        } catch (Exception ignored) {
        }
    }
}


