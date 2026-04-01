package com.hospital.billing.controller;

import com.hospital.billing.dto.integration.ClinicalCompletedRequest;
import com.hospital.billing.dto.integration.ClinicalCompletedResult;
import com.hospital.billing.facade.BillingFacade;
import com.hospital.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
public class BillingIntegrationController {

    private final BillingFacade billingFacade;

    public BillingIntegrationController(BillingFacade billingFacade) {
        this.billingFacade = billingFacade;
    }

    @PostMapping("/claims")
    public ResponseEntity<ApiResponse<ClinicalCompletedResult>> handleClinicalCompleted(
            @RequestBody ClinicalCompletedRequest request
    ) {
        ClinicalCompletedResult result = billingFacade.handleClinicalCompleted(request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "청구 생성 요청이 정상적으로 접수되었습니다.",
                        result
                )
        );
    }
}