package com.hospital.billing.controller;

import com.hospital.billing.dto.BillSummaryResponse;
import com.hospital.billing.entity.BillingStatus;
import com.hospital.billing.service.BillingListQueryService;
import com.hospital.common.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
public class BillingListController {

    private final BillingListQueryService billingListQueryService;

    public BillingListController(BillingListQueryService billingListQueryService) {
        this.billingListQueryService = billingListQueryService;
    }

    /**
     * OB1-63
     * 환자 기준 청구 목록 조회
     */
    @GetMapping("/patients/{patientId}/bills")
    public ApiResponse<List<BillSummaryResponse>> getBillsByPatient(
            @PathVariable Long patientId,
            @RequestParam(required = false) BillingStatus status
    ) {
        List<BillSummaryResponse> result = billingListQueryService.getBillsByPatient(patientId, status);
        return ApiResponse.success(result, "환자 기준 청구 목록 조회 성공");
    }

    /**
     * 대시보드/목록용: 전체 청구 목록 조회
     * - status가 넘어오면 상태별 필터 적용
     * 예) GET /api/billing/bills?status=READY
     */
    @GetMapping("/bills")
    public ApiResponse<List<BillSummaryResponse>> getBills(
            @RequestParam(required = false) BillingStatus status
    ) {
        List<BillSummaryResponse> result = billingListQueryService.getBills(status);
        return ApiResponse.success(result, "전체 청구 목록 조회 성공");
    }

    /**
     * OB1-63
     * 내원 기준 청구 목록 조회
     */
    @GetMapping("/encounters/{encounterId}/bills")
    public ApiResponse<List<BillSummaryResponse>> getBillsByEncounter(
            @PathVariable Long encounterId
    ) {
        List<BillSummaryResponse> result = billingListQueryService.getBillsByEncounter(encounterId);
        return ApiResponse.success(result, "내원 기준 청구 목록 조회 성공");
    }
    //미수금
    @GetMapping("/outstanding")
    public ApiResponse<List<BillSummaryResponse>> getOutstandingBills() {
        List<BillSummaryResponse> result = billingListQueryService.getOutstandingBills();
        return ApiResponse.success(result, "미수금 청구 목록 조회 성공");
    }
}