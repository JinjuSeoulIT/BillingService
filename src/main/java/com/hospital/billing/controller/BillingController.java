package com.hospital.billing.controller;

import com.hospital.billing.dto.BillDetailResponse;
import com.hospital.billing.dto.BillItemResponse;
import com.hospital.billing.dto.BillingStatsResponse;
import com.hospital.billing.entity.Bill;
import com.hospital.billing.repository.BillRepository;
import com.hospital.billing.service.BillingItemQueryService;
import com.hospital.billing.service.BillingStatsQueryService;
import com.hospital.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "청구/통계(Billing)", description = "청구 상세 및 수납 통계 조회 API")
@CrossOrigin(origins = "http://localhost:3001")
@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillRepository billRepository;
    private final BillingStatsQueryService billingStatsQueryService;

    // 청구 상세 조회 시 항목 목록까지 함께 조회하기 위한 서비스 주입
    private final BillingItemQueryService billingItemQueryService;

    public BillingController(BillRepository billRepository,
                             BillingStatsQueryService billingStatsQueryService,
                             BillingItemQueryService billingItemQueryService) {
        this.billRepository = billRepository;
        this.billingStatsQueryService = billingStatsQueryService;
        this.billingItemQueryService = billingItemQueryService;
    }

    @Operation(
            summary = "청구 상세 조회",
            description = "billId 기준으로 청구 상세 정보(총액, 결제금액, 잔액, 상태, 항목 목록 등)를 조회합니다."
    )
    @GetMapping("/bills/{billId}")
    public ApiResponse<BillDetailResponse> getBillDetail(
            @PathVariable Long billId
    ) {

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("청구 정보가 없습니다."));

        // 현재 Bill 기준으로 BILL_ITEM 목록도 함께 조회
        List<BillItemResponse> billItems = billingItemQueryService.getBillItemDetails(billId);

        // 기존 new BillDetailResponse(bill) → 항목 목록 포함 생성자로 변경
        BillDetailResponse response = new BillDetailResponse(bill, billItems);

        return ApiResponse.success(response, "청구 상세 조회 성공");
    }

    @Operation(
            summary = "수납 통계 조회",
            description = "READY/CONFIRMED/PAID 상태 건수 및 금일/전체 수납 금액을 조회합니다."
    )
    @GetMapping("/stats")
    public ApiResponse<BillingStatsResponse> getBillingStats() {
        BillingStatsResponse stats = billingStatsQueryService.getStats();
        return ApiResponse.success(stats, "수납 통계 조회 성공");
    }
}