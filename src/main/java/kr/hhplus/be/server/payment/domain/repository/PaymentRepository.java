package kr.hhplus.be.server.payment.domain.repository;

import kr.hhplus.be.server.payment.domain.entity.PaymentEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    PaymentEntity save(PaymentEntity payment);

    Optional<PaymentEntity> findById(Long aLong);
} 