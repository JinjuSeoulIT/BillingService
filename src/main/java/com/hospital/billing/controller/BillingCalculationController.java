package com.hospital.billing.controller;

import com.hospital.billing.dto.CalculatedBillResponse;
import com.hospital.billing.service.BillingCalculationQueryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing/bills")
public class BillingCalculationController {

    private final BillingCalculationQueryService billingCalculationQueryService;

    public BillingCalculationController(BillingCalculationQueryService billingCalculationQueryService) {
        this.billingCalculationQueryService = billingCalculationQueryService;
    }

    /**
     * OB1-61
     * 자동 계산된 진료비 조회
     */
    @GetMapping("/{billId}/calculated")
    public CalculatedBillResponse getCalculatedBill(@PathVariable Long billId) {
        return billingCalculationQueryService.getCalculatedBill(billId);
    }
}
