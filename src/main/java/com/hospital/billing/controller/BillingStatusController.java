package com.hospital.billing.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hospital.billing.dto.BillStatusResponse;
import com.hospital.billing.service.BillingStatusQueryService;
import org.springframework.web.bind.annotation.*;
import com.hospital.common.response.ApiResponse;

@RestController
@RequestMapping("/api/billing/bills")
public class BillingStatusController {

    private final BillingStatusQueryService billingStatusQueryService;

    public BillingStatusController(BillingStatusQueryService billingStatusQueryService) {
        this.billingStatusQueryService = billingStatusQueryService;
    }
    private static final Logger log =
            LoggerFactory.getLogger(BillingStatusController.class);

    /**
     * OB1-59
     * 청구 상태 조회
     */
    @GetMapping("/{billId}/status")
    public ApiResponse<BillStatusResponse> getBillStatus(@PathVariable Long billId) {
        log.info("GET /api/billing/bills/{}/status 요청", billId);
        BillStatusResponse response =
                billingStatusQueryService.getBillStatus(billId);

        return ApiResponse.success(response, "청구 상태 조회 성공");
    }
}
