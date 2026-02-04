package com.hospital.billing.service;

import com.hospital.billing.dto.BillHistoryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillingHistoryQueryService {

    /**
     * OB1-60
     * 청구 이력 조회 (뼈대)
     */
    public List<BillHistoryResponse> getBillHistory(Long billId) {
        // TODO: 통합 시점에 BillHistory 테이블/로그 기반으로 구현
        return List.of();
    }
}
