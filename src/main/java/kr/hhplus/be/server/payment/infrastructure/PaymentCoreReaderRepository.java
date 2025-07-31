package kr.hhplus.be.server.payment.infrastructure;

import kr.hhplus.be.server.payment.domain.entity.PaymentEntity;
import kr.hhplus.be.server.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentCoreReaderRepository implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public PaymentEntity save(PaymentEntity payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Optional<PaymentEntity> findById(Long id) {
        return paymentJpaRepository.findById(id);
    }
} 