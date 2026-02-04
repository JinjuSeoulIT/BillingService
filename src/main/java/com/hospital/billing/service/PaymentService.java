package com.hospital.billing.service;

import com.hospital.billing.entity.Payment;
import com.hospital.billing.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    // 생성자 주입
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * 결제 생성
     */
    public Payment createPayment(Long patientId, Integer amount) {
        Payment payment = new Payment(
                patientId,
                amount,
                "PAID"
        );
        return paymentRepository.save(payment);
    }

    /**
     * 결제 전체 조회
     */
    public List<Payment> getPayments() {
        return paymentRepository.findAll();
    }
}
