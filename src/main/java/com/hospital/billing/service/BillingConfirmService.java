package com.hospital.billing.service;

import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.BillingStatus;
import com.hospital.billing.repository.BillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingConfirmService {

    private final BillRepository billRepository;

    public BillingConfirmService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    /**
     * OB1-55
     * 청구 금액 확정
     */
    @Transactional
    public Bill confirm(Long billId) {

        // 1. 청구 조회
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("청구 정보를 찾을 수 없습니다."));

        // 2. 상태 검증
        if (bill.getStatus() != BillingStatus.READY) {
            throw new IllegalStateException("확정 가능한 상태가 아닙니다.");
        }

        // 3. 상태 변경 (READY → CONFIRMED)
        bill.setStatus(BillingStatus.CONFIRMED);

        // 4. 저장
        return billRepository.save(bill);
    }
}
