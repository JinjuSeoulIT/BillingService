package com.hospital.billing.paymentmethod.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "PAYMENT_METHOD_MASTER")
public class PaymentMethodMaster {

    @Id
    @Column(name = "METHOD_CODE")
    private String methodCode;

    @Column(name = "METHOD_NAME")
    private String methodName;

    @Column(name = "USE_YN")
    private String useYn;

    public String getMethodCode() {
        return methodCode;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getUseYn() {
        return useYn;
    }
}