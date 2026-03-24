package com.hospital.billing.exception;

public class PaymentNotFoundException extends BillingException {

    public PaymentNotFoundException(Long paymentId) {
        super("결제를 찾을 수 없습니다. paymentId=" + paymentId);
    }
}