package com.hospital.billing.controller;

import com.hospital.billing.entity.Payment;
import com.hospital.billing.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    // 생성자 주입
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 결제 생성
     * POST /payments?patientId=1&amount=10000
     */
    @PostMapping
    public Payment createPayment(
            @RequestParam Long patientId,
            @RequestParam Integer amount
    ) {
        return paymentService.createPayment(patientId, amount);
    }

    /**
     * 결제 전체 조회
     * GET /payments
     */
    @GetMapping
    public List<Payment> getPayments() {
        return paymentService.getPayments();
    }
}
