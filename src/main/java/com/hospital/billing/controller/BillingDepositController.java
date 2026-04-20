package com.hospital.billing.controller;

import com.hospital.billing.dto.deposit.BillingDepositCreateRequest;
import com.hospital.billing.dto.deposit.BillingDepositResponse;
import com.hospital.billing.service.BillingDepositService;
import com.hospital.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "선수금(Billing Deposits)", description = "환자 기준 선수금 등록 및 조회 API")
@CrossOrigin(origins = "http://localhost:3001")
@RestController
@RequestMapping("/api/billing/deposits")
public class BillingDepositController {

    private final BillingDepositService billingDepositService;

    public BillingDepositController(BillingDepositService billingDepositService) {
        this.billingDepositService = billingDepositService;
    }

    @Operation(summary = "선수금 등록", description = "환자 기준으로 선수금을 등록합니다.")
    @PostMapping
    public ApiResponse<BillingDepositResponse> createDeposit(
            @RequestBody BillingDepositCreateRequest request
    ) {
        BillingDepositResponse response = billingDepositService.createDeposit(request);
        return ApiResponse.success(response, "선수금 등록 성공");
    }

    @Operation(summary = "선수금 목록 조회", description = "전체 또는 환자 기준 선수금 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<List<BillingDepositResponse>> getDeposits(
            @Parameter(description = "환자 ID", example = "7")
            @RequestParam(required = false) Long patientId
    ) {
        return ApiResponse.success(
                billingDepositService.getDeposits(patientId),
                "선수금 목록 조회 성공"
        );
    }
}
