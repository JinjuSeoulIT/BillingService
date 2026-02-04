package com.hospital.billing.service;

import com.hospital.billing.dto.BillItemResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillingItemQueryService {

    /**
     * 특정 Bill에 대한 항목별 청구 금액 상세 조회
     * (OB1-62 뼈대)
     */
    public List<BillItemResponse> getBillItemDetails(Long billId) {
        // TODO: 통합 시점에 BillItem 조회 + DTO 변환 로직 구현
        return List.of();
    }
}
