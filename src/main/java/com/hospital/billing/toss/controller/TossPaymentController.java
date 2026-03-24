package com.hospital.billing.toss.controller;

import com.hospital.billing.toss.dto.TossApproveRequest;
import com.hospital.billing.toss.dto.TossApproveResponse;
import com.hospital.billing.toss.service.TossPaymentService;
import com.hospital.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing/toss")
public class TossPaymentController {

    private final TossPaymentService tossPaymentService;

    public TossPaymentController(TossPaymentService tossPaymentService) {
        this.tossPaymentService = tossPaymentService;
    }

    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<TossApproveResponse>> approvePayment(
            @RequestBody TossApproveRequest request
    ) {
        try {
            TossApproveResponse result = tossPaymentService.approvePayment(request);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "토스 결제 승인이 완료되었습니다.",
                            result
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(
                            false,
                            e.getMessage(),
                            null
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(
                            false,
                            "토스 결제 승인 중 오류가 발생했습니다. " + e.getClass().getSimpleName() + ": " + e.getMessage(),
                            null
                    )
            );
        }
    }
}