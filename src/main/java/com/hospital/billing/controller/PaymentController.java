package com.hospital.billing.controller;

import com.hospital.billing.dto.PaymentResponse;
import com.hospital.billing.entity.PaymentMethod; // [추가]
import com.hospital.billing.service.PaymentService;
import com.hospital.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "수납/결제(Payments)",
        description = "수납 생성/취소, 부분 환불, 청구 기준 결제 내역 조회 API"
)
@CrossOrigin(origins = "http://localhost:3001")
@RestController
@RequestMapping("/api/billing/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 수납 생성
     */
    @Operation(
            summary = "수납 생성",
            description = "청구(billId)에 대해 결제 금액(amount)만큼 수납을 생성합니다. (부분 수납 가능)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수납 처리 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청(금액 오류/청구 없음 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "상태 충돌(이미 수납 완료 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ApiResponse<PaymentResponse> createPayment(
            @Parameter(description = "청구 ID", example = "1", required = true)
            @RequestParam Long billId,

            @Parameter(description = "수납 금액", example = "10000", required = true)
            @RequestParam Integer amount,

            // [추가] 결제 수단
            @Parameter(description = "결제 수단", example = "CARD", required = true)
            @RequestParam PaymentMethod method
    ) {
        PaymentResponse payment = paymentService.createPayment(billId, amount, method);
        return ApiResponse.success(payment, "수납 처리 성공");
    }

    /**
     * 수납 취소
     */
    @Operation(
            summary = "수납 취소(전체 취소)",
            description = "paymentId 기준으로 결제를 취소합니다. 취소 시 청구의 paidAmount/remainingAmount 및 상태가 재계산됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수납 취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청(paymentId 없음 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "상태 충돌(이미 취소된 결제 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/{paymentId}/cancel")
    public ApiResponse<Void> cancelPayment(
            @Parameter(description = "결제 ID", example = "22", required = true)
            @PathVariable Long paymentId
    ) {

        paymentService.cancelPayment(paymentId);

        return ApiResponse.success(null, "수납 취소 성공");
    }

    /**
     * 부분 환불
     */
    @Operation(
            summary = "부분 환불",
            description = "paymentId 기준으로 결제 금액 중 amount 만큼 부분 환불합니다. 환불 시 청구의 paidAmount/remainingAmount 및 상태가 재계산됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "환불 처리 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청(환불 금액 오류/paymentId 없음 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "상태 충돌(환불 불가 상태 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/{paymentId}/refund")
    public ApiResponse<PaymentResponse> refundPayment(
            @Parameter(description = "결제 ID", example = "22", required = true)
            @PathVariable Long paymentId,

            @Parameter(description = "환불 금액", example = "5000", required = true)
            @RequestParam Integer amount
    ) {
        PaymentResponse response = paymentService.refundPayment(paymentId, amount);
        return ApiResponse.success(response, "환불 처리 성공");
    }

    /**
     * 수납 전체 조회
     */
    @Operation(
            summary = "수납 전체 조회",
            description = "전체 결제 내역을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수납 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ApiResponse<List<PaymentResponse>> getPayments() {
        return ApiResponse.success(
                paymentService.getPaymentsAsResponse(),
                "수납 목록 조회 성공"
        );
    }

    /**
     * 추가: 청구 기준 결제 내역 조회
     * GET /api/billing/payments/bill/{billId}
     */
    @Operation(
            summary = "청구 기준 결제 내역 조회",
            description = "billId 기준으로 결제 내역을 최신순으로 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "청구별 수납 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청(청구 ID 오류 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/bill/{billId}")
    public ApiResponse<List<PaymentResponse>> getPaymentsByBill(
            @Parameter(description = "청구 ID", example = "1", required = true)
            @PathVariable Long billId
    ) {
        return ApiResponse.success(
                paymentService.getPaymentsByBill(billId),
                "청구별 수납 목록 조회 성공"
        );
    }
}