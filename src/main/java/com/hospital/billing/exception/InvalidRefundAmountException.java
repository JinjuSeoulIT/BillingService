package com.hospital.billing.exception;

public class InvalidRefundAmountException extends BillingException {

    public InvalidRefundAmountException(String message) {
        super(message);
    }
}