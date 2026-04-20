package com.hospital.billing.paymentmethod.service;

import com.hospital.billing.paymentmethod.entity.PaymentMethodMaster;
import com.hospital.billing.paymentmethod.repository.PaymentMethodMasterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentMethodService {

    private final PaymentMethodMasterRepository repository;

    public PaymentMethodService(PaymentMethodMasterRepository repository) {
        this.repository = repository;
    }

    public List<PaymentMethodMaster> getActiveMethods() {
        return repository.findByUseYn("Y");
    }
}