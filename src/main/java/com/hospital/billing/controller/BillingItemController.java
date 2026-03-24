package com.hospital.billing.controller;

import com.hospital.billing.dto.BillItemResponse;
import com.hospital.billing.service.BillingItemQueryService;
import com.hospital.common.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing/bills")
public class BillingItemController {

    private final BillingItemQueryService billingItemQueryService;

    public BillingItemController(BillingItemQueryService billingItemQueryService) {
        this.billingItemQueryService = billingItemQueryService;
    }

    /**
     * OB1-62
     * 항목별 청구금액 상세 조회
     */
    @GetMapping("/{billId}/items")
    public ApiResponse<List<BillItemResponse>> getBillItemDetails(
            @PathVariable Long billId
    ) {
        List<BillItemResponse> result =
                billingItemQueryService.getBillItemDetails(billId);

        return ApiResponse.success(result, "항목별 청구금액 상세 조회 성공");
    }
}
