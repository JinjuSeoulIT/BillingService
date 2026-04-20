package com.hospital.billing.paymentmethod.controller;

import com.hospital.billing.paymentmethod.entity.PaymentMethodMaster;
import com.hospital.billing.paymentmethod.service.PaymentMethodService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing/payment-methods")
@CrossOrigin(origins = "http://localhost:3001")
public class PaymentMethodController {

    private final PaymentMethodService service;

    public PaymentMethodController(PaymentMethodService service) {
        this.service = service;
    }

    @GetMapping
    public List<PaymentMethodMaster> getMethods() {
        return service.getActiveMethods();
    }
}