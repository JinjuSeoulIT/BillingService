package com.hospital.billing.service;

import com.hospital.billing.dto.BillSummaryResponse;
import com.hospital.billing.entity.Bill;
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
     */
    public List<BillSummaryResponse> getBillsByPatient(Long patientId) {
        List<Bill> bills = billRepository.findByPatientId(patientId);

        // 임시: Bill → BillSummaryResponse 단순 변환
        return bills.stream()
                .map(bill -> new BillSummaryResponse(
                        bill.getId(),
                        bill.getPatientId(),
                        bill.getTreatmentDate(),
                        bill.getTotalAmount()
                ))
                .toList();
    }

    /**
     * OB1-63
     * 내원(Encounter) 기준 청구 목록 조회 (아직 미구현)
     */
    public List<BillSummaryResponse> getBillsByEncounter(Long encounterId) {
        return List.of();
    }
}
