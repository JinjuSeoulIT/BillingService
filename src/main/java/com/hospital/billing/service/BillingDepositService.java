package com.hospital.billing.service;

import com.hospital.billing.dto.deposit.BillingDepositCreateRequest;
import com.hospital.billing.dto.deposit.BillingDepositResponse;
import com.hospital.billing.entity.BillingDeposit;
import com.hospital.billing.payment.entity.PaymentMethod;
import com.hospital.billing.repository.BillingDepositRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BillingDepositService {

    private final BillingDepositRepository billingDepositRepository;

    public BillingDepositService(BillingDepositRepository billingDepositRepository) {
        this.billingDepositRepository = billingDepositRepository;
    }

    @Transactional
    public BillingDepositResponse createDeposit(BillingDepositCreateRequest request) {
        validateCreateRequest(request);

        Timestamp receivedAt = request.getReceivedAt() != null
                ? Timestamp.valueOf(request.getReceivedAt())
                : new Timestamp(System.currentTimeMillis());

        BillingDeposit entity = BillingDeposit.create(
                request.getPatientId(),
                request.getDepositAmount(),
                request.getPaymentMethod(),
                normalizeMemo(request.getDepositMemo()),
                receivedAt
        );

        BillingDeposit saved = billingDepositRepository.save(entity);
        return BillingDepositResponse.from(saved);
    }

    public List<BillingDepositResponse> getDeposits(Long patientId) {
        List<BillingDeposit> deposits = patientId != null
                ? billingDepositRepository.findByPatientIdOrderByReceivedAtDescDepositIdDesc(patientId)
                : billingDepositRepository.findAllByOrderByReceivedAtDescDepositIdDesc();

        return deposits.stream()
                .map(BillingDepositResponse::from)
                .toList();
    }

    private void validateCreateRequest(BillingDepositCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("선수금 등록 요청이 없습니다.");
        }

        if (request.getPatientId() == null || request.getPatientId() <= 0) {
            throw new IllegalArgumentException("유효한 환자 ID가 필요합니다.");
        }

        if (request.getDepositAmount() == null || request.getDepositAmount() <= 0) {
            throw new IllegalArgumentException("선수금 금액은 0보다 커야 합니다.");
        }

        if (request.getPaymentMethod() == null) {
            throw new IllegalArgumentException("결제 수단을 선택해주세요.");
        }

        if (request.getPaymentMethod() == PaymentMethod.INSURANCE) {
            throw new IllegalArgumentException("선수금 등록에는 보험 결제 수단을 사용할 수 없습니다.");
        }
    }

    private String normalizeMemo(String memo) {
        if (memo == null) {
            return null;
        }

        String trimmed = memo.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
