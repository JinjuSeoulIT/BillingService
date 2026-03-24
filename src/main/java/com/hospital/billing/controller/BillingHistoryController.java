package com.hospital.billing.controller;

import com.hospital.billing.dto.BillHistoryResponse;
import com.hospital.billing.service.BillingHistoryQueryService;
import com.hospital.common.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing/bills")
public class BillingHistoryController {

    private final BillingHistoryQueryService billingHistoryQueryService;

    public BillingHistoryController(BillingHistoryQueryService billingHistoryQueryService) {
        this.billingHistoryQueryService = billingHistoryQueryService;
    }

    /**
     * OB1-60
     * 청구 이력 조회
     */
    @GetMapping("/{billId}/history")
    public ApiResponse<List<BillHistoryResponse>> getBillHistory(
            @PathVariable Long billId
    ) {
        List<BillHistoryResponse> result =
                billingHistoryQueryService.getBillHistory(billId);

        return ApiResponse.success(result, "청구 이력 조회 성공");
    }
}
