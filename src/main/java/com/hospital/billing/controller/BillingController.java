package com.hospital.billing.controller;

import com.hospital.billing.dto.BillCancelResponse;
import com.hospital.billing.dto.BillConfirmResponse;
import com.hospital.billing.dto.BillDetailResponse;
import com.hospital.billing.dto.BillHistoryResponse;
import com.hospital.billing.dto.BillItemResponse;
import com.hospital.billing.dto.BillStatusResponse;
import com.hospital.billing.dto.BillSummaryResponse;
import com.hospital.billing.dto.BillingStatsResponse;
import com.hospital.billing.dto.CalculatedBillResponse;
import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.BillingStatus;
import com.hospital.billing.service.BillingService;
import com.hospital.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "청구/통계(Billing)", description = "청구 상세, 상태 변경, 목록, 항목, 이력, 통계 조회 API")
@CrossOrigin(origins = "http://localhost:3001")
@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private static final Logger log =
            LoggerFactory.getLogger(BillingController.class);

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/bills/{billId}/confirm")
    public ApiResponse<BillConfirmResponse> confirm(
            @PathVariable Long billId
    ) {
        Bill bill = billingService.confirm(billId);

        return ApiResponse.success(
                new BillConfirmResponse(
                        bill.getId(),
                        bill.getStatus().name()
                ),
                "청구 금액이 확정되었습니다."
        );
    }

    @PostMapping("/bills/{billId}/cancel")
    public ApiResponse<BillCancelResponse> cancel(
            @PathVariable Long billId
    ) {
        Bill bill = billingService.cancel(billId);

        return ApiResponse.success(
                new BillCancelResponse(
                        bill.getId(),
                        bill.getStatus().name()
                ),
                "청구가 취소되었습니다."
        );
    }

    @PostMapping("/bills/{billId}/unconfirm")
    public ApiResponse<BillConfirmResponse> unconfirm(
            @PathVariable Long billId
    ) {
        Bill bill = billingService.unconfirm(billId);

        return ApiResponse.success(
                new BillConfirmResponse(
                        bill.getId(),
                        bill.getStatus().name()
                ),
                "청구 확정이 해제되었습니다."
        );
    }

    @PostMapping("/bills/{billId}/restore")
    public ApiResponse<BillConfirmResponse> restore(
            @PathVariable Long billId
    ) {
        Bill bill = billingService.restore(billId);

        return ApiResponse.success(
                new BillConfirmResponse(
                        bill.getId(),
                        bill.getStatus().name()
                ),
                "청구가 복원되었습니다."
        );
    }

    @Operation(
            summary = "청구 상세 조회",
            description = "billId 기준으로 청구 상세 정보(총액, 결제금액, 잔액, 상태, 항목 목록 등)를 조회합니다."
    )
    @GetMapping("/bills/{billId}")
    public ApiResponse<BillDetailResponse> getBillDetail(
            @PathVariable Long billId
    ) {
        BillDetailResponse response = billingService.getBillDetail(billId);
        return ApiResponse.success(response, "청구 상세 조회 성공");
    }

    @GetMapping("/bills/{billId}/status")
    public ApiResponse<BillStatusResponse> getBillStatus(
            @PathVariable Long billId
    ) {
        log.info("GET /api/billing/bills/{}/status 요청", billId);

        BillStatusResponse response = billingService.getBillStatus(billId);

        return ApiResponse.success(response, "청구 상태 조회 성공");
    }

    @GetMapping("/bills/{billId}/history")
    public ApiResponse<List<BillHistoryResponse>> getBillHistory(
            @PathVariable Long billId
    ) {
        List<BillHistoryResponse> result =
                billingService.getBillHistory(billId);

        return ApiResponse.success(result, "청구 이력 조회 성공");
    }

    @GetMapping("/bills/{billId}/calculated")
    public ApiResponse<CalculatedBillResponse> getCalculatedBill(
            @PathVariable Long billId
    ) {
        CalculatedBillResponse result =
                billingService.getCalculatedBill(billId);

        return ApiResponse.success(result, "자동 계산된 진료비 조회 성공");
    }

    @GetMapping("/bills/{billId}/items")
    public ApiResponse<List<BillItemResponse>> getBillItemDetails(
            @PathVariable Long billId
    ) {
        List<BillItemResponse> result =
                billingService.getBillItemDetails(billId);

        return ApiResponse.success(result, "항목별 청구금액 상세 조회 성공");
    }

    @GetMapping("/patients/{patientId}/bills")
    public ApiResponse<List<BillSummaryResponse>> getBillsByPatient(
            @PathVariable Long patientId,
            @RequestParam(required = false) BillingStatus status,
            @RequestParam(required = false, defaultValue = "false") boolean confirmedOnly,
            @RequestParam(required = false, defaultValue = "false") boolean partialOnly
    ) {
        List<BillSummaryResponse> result =
                billingService.getBillsByPatient(patientId, status, confirmedOnly, partialOnly);

        return ApiResponse.success(result, "환자 기준 청구 목록 조회 성공");
    }

    /**
     * [수정] billingDate 선택 파라미터 추가
     */
    @GetMapping("/bills")
    public ApiResponse<List<BillSummaryResponse>> getBills(
            @RequestParam(required = false) BillingStatus status,
            @RequestParam(required = false, defaultValue = "false") boolean confirmedOnly,
            @RequestParam(required = false, defaultValue = "false") boolean partialOnly,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate billingDate
    ) {
        List<BillSummaryResponse> result =
                billingService.getBills(status, confirmedOnly, partialOnly, billingDate);

        return ApiResponse.success(result, "전체 청구 목록 조회 성공");
    }

    @GetMapping("/encounters/{encounterId}/bills")
    public ApiResponse<List<BillSummaryResponse>> getBillsByEncounter(
            @PathVariable Long encounterId
    ) {
        List<BillSummaryResponse> result =
                billingService.getBillsByEncounter(encounterId);

        return ApiResponse.success(result, "내원 기준 청구 목록 조회 성공");
    }

    @GetMapping("/outstanding")
    public ApiResponse<List<BillSummaryResponse>> getOutstandingBills() {
        List<BillSummaryResponse> result =
                billingService.getOutstandingBills();

        return ApiResponse.success(result, "미수금 청구 목록 조회 성공");
    }

    @Operation(
            summary = "수납 통계 조회",
            description = "READY/CONFIRMED/PAID 상태 건수 및 금일/전체 수납 금액을 조회합니다."
    )
    @GetMapping("/stats")
    public ApiResponse<BillingStatsResponse> getBillingStats() {
        BillingStatsResponse stats = billingService.getStats();
        return ApiResponse.success(stats, "수납 통계 조회 성공");
    }
}