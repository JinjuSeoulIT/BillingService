package com.hospital.billing.controller;

import com.hospital.billing.dto.BillConfirmResponse;
import com.hospital.billing.entity.Bill;
import com.hospital.billing.service.BillingConfirmService;
import com.hospital.common.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
public class BillingConfirmController {

    private final BillingConfirmService billingConfirmService;

    public BillingConfirmController(BillingConfirmService billingConfirmService) {
        this.billingConfirmService = billingConfirmService;
    }

    /**
     * OB1-55
     * 청구 금액 확정
     */
    @PostMapping("/bills/{billId}/confirm")
    public ApiResponse<BillConfirmResponse> confirm(
            @PathVariable Long billId
    ) {
        Bill bill = billingConfirmService.confirm(billId);

        return ApiResponse.success(
                new BillConfirmResponse(
                        bill.getId(),
                        bill.getStatus().name()
                ),
                "청구 금액이 확정되었습니다."
        );
    }
}
