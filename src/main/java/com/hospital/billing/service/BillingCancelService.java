package com.hospital.billing.service;

import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.BillingStatus;
import com.hospital.billing.repository.BillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingCancelService {

    private final BillRepository billRepository;

    public BillingCancelService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    /**
     * OB1-56
     * 청구 취소 / 정정
     */
    @Transactional
    public Bill cancel(Long billId) {

        // 1. 청구 조회
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("청구 정보를 찾을 수 없습니다."));

        // 2. 상태 검증 (CONFIRMED만 취소 가능)
        if (bill.getStatus() != BillingStatus.CONFIRMED) {
            throw new IllegalStateException("확정된 청구만 취소할 수 있습니다.");
        }

        // 3. 상태 변경 (CONFIRMED → CANCELED)
        bill.setStatus(BillingStatus.CANCELED);

        // 4. 저장
        return billRepository.save(bill);
    }
}
