package com.hospital.billing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hospital.billing.dto.BillStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class BillingStatusQueryService {

    /**
     * OB1-59
     * 청구 상태 조회 (뼈대)
     */
    public BillStatusResponse getBillStatus(Long billId) {
        log.info("청구 상태 조회 처리 시작 - billId={}", billId);
        // TODO: 통합 시 Payment/상태 테이블 연동
        return new BillStatusResponse(billId, "UNKNOWN");
    }
    private static final Logger log =
            LoggerFactory.getLogger(BillingStatusQueryService.class);
}

