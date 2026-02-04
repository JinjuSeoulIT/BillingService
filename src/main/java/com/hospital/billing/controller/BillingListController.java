package com.hospital.billing.controller;

import com.hospital.billing.dto.BillSummaryResponse;
import com.hospital.billing.service.BillingListQueryService;
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
    public List<BillSummaryResponse> getBillsByPatient(
            @PathVariable Long patientId
    ) {
        return billingListQueryService.getBillsByPatient(patientId);
    }

    /**
     * OB1-63
     * 내원 기준 청구 목록 조회
     */
    @GetMapping("/encounters/{encounterId}/bills")
    public List<BillSummaryResponse> getBillsByEncounter(
            @PathVariable Long encounterId
    ) {
        return billingListQueryService.getBillsByEncounter(encounterId);
    }
}
