package com.hospital.billing.service;

import com.hospital.billing.dto.CalculatedBillResponse;
import org.springframework.stereotype.Service;

@Service
public class BillingCalculationQueryService {

    /**
     * OB1-61
     * 자동 계산된 진료비 조회 (뼈대)
     */
    public CalculatedBillResponse getCalculatedBill(Long billId) {
        // TODO: 통합 시점에 수가/보험/할인 계산 로직 구현

        // 더미 계산 결과
        int originalAmount = 30000;
        int calculatedAmount = 25000;

        return new CalculatedBillResponse(
                billId,
                originalAmount,
                calculatedAmount,
                "자동 계산 로직은 통합 시점에 구현됩니다."
        );
    }
}
