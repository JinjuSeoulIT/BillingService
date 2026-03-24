package com.hospital.billing.toss.service;

import com.hospital.billing.entity.PaymentMethod;
import com.hospital.billing.service.PaymentService;
import com.hospital.billing.toss.client.TossPaymentClient;
import com.hospital.billing.toss.dto.TossApproveRequest;
import com.hospital.billing.toss.dto.TossApproveResponse;
import org.springframework.stereotype.Service;

@Service
public class TossPaymentService {

    private final TossPaymentClient tossPaymentClient;
    private final PaymentService paymentService;

    public TossPaymentService(TossPaymentClient tossPaymentClient,
                              PaymentService paymentService) {
        this.tossPaymentClient = tossPaymentClient;
        this.paymentService = paymentService;
    }

    public TossApproveResponse approvePayment(TossApproveRequest request) {
        validateRequest(request);

        TossApproveResponse response = tossPaymentClient.confirmPayment(request);

        Integer paymentAmount = convertAmountToInteger(request.getAmount());

        paymentService.createPayment(
                request.getBillId(),
                paymentAmount,
                PaymentMethod.CARD
        );

        return response;
    }

    private void validateRequest(TossApproveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("토스 승인 요청 정보가 없습니다.");
        }

        if (request.getPaymentKey() == null || request.getPaymentKey().isBlank()) {
            throw new IllegalArgumentException("paymentKey는 필수입니다.");
        }

        if (request.getOrderId() == null || request.getOrderId().isBlank()) {
            throw new IllegalArgumentException("orderId는 필수입니다.");
        }

        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("amount는 0보다 커야 합니다.");
        }

        if (request.getBillId() == null || request.getBillId() <= 0) {
            throw new IllegalArgumentException("billId는 0보다 커야 합니다.");
        }
    }

    private Integer convertAmountToInteger(Long amount) {
        if (amount == null) {
            throw new IllegalArgumentException("amount는 필수입니다.");
        }

        if (amount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("amount가 Integer 범위를 초과했습니다.");
        }

        return amount.intValue();
    }
}