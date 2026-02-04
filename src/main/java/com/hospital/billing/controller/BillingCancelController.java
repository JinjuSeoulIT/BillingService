package com.hospital.billing.controller;

import com.hospital.billing.dto.BillCancelResponse;
import com.hospital.billing.entity.Bill;
import com.hospital.billing.service.BillingCancelService;
import com.hospital.common.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
public class BillingCancelController {

    private final BillingCancelService billingCancelService;

    public BillingCancelController(BillingCancelService billingCancelService) {
        this.billingCancelService = billingCancelService;
    }

    /**
     * OB1-56
     * 청구 취소 / 정정
     */
    @PostMapping("/bills/{billId}/cancel")
    public ApiResponse<BillCancelResponse> cancel(
            @PathVariable Long billId
    ) {
        Bill bill = billingCancelService.cancel(billId);

        return ApiResponse.success(
                new BillCancelResponse(
                        bill.getId(),
                        bill.getStatus().name()
                ),
                "청구가 취소되었습니다."
        );
    }
}
