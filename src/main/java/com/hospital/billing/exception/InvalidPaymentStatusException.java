package com.hospital.billing.exception;

public class InvalidPaymentStatusException extends BillingException {

    public InvalidPaymentStatusException(String message) {
        super(message);
    }
}