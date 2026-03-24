package com.hospital.billing.service;

import com.hospital.billing.dto.BillSummaryResponse;
import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.BillingStatus;
import com.hospital.billing.repository.BillRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillingListQueryService {

    private final BillRepository billRepository;

    public BillingListQueryService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    /**
     * OB1-63
     * 환자 기준 청구 목록 조회
     * - status가 넘어오면 상태별 필터 적용
     */
    public List<BillSummaryResponse> getBillsByPatient(Long patientId, BillingStatus status) {
        final List<Bill> bills;
        if (status == null) {
            bills = billRepository.findByPatientId(patientId);
        } else {
            bills = billRepository.findByPatientIdAndStatus(patientId, status);
        }
        return bills.stream()
                .map(BillSummaryResponse::new)
                .toList();
    }

    /** 전체 청구 목록 조회 */

    public List<BillSummaryResponse> getBills(BillingStatus status) {

        final List<Bill> bills;

        if (status == null) {
            bills = billRepository.findAll();
        } else {
            bills = billRepository.findByStatus(status);
        }

        return bills.stream()
                .map(BillSummaryResponse::new)
                .toList();
    }
    //미수금
    public List<BillSummaryResponse> getOutstandingBills() {
        return billRepository.findByRemainingAmountGreaterThan(0)
                    .stream()
                    .map(BillSummaryResponse::new)
                    .toList();
        }

    // OB1-63 내원 기준 청구 목록 조회 (미구현)
    public List<BillSummaryResponse> getBillsByEncounter(Long encounterId) {
        return List.of();
    }
}