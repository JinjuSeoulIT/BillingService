package com.hospital.billing.service;

import com.hospital.billing.dto.PaymentResponse;
import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.BillingStatus;
import com.hospital.billing.entity.Payment;
import com.hospital.billing.entity.PaymentMethod;
import com.hospital.billing.entity.PaymentStatus;
import com.hospital.billing.exception.InvalidPaymentStatusException;
import com.hospital.billing.exception.InvalidRefundAmountException;
import com.hospital.billing.exception.PaymentNotFoundException;
import com.hospital.billing.repository.BillRepository;
import com.hospital.billing.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          BillRepository billRepository) {
        this.paymentRepository = paymentRepository;
        this.billRepository = billRepository;
    }

    /**
     * 수납 생성 (자동 계산 포함)
     */
    public PaymentResponse createPayment(Long billId, Integer amount, PaymentMethod method) {

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() ->
                        new IllegalArgumentException("청구가 존재하지 않습니다. billId=" + billId));

        if (bill.getStatus() == BillingStatus.PAID) {
            throw new IllegalStateException("이미 수납 완료된 청구입니다.");
        }

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다.");
        }

        if (amount > bill.getRemainingAmount()) {
            throw new IllegalArgumentException("결제 금액이 남은 금액보다 클 수 없습니다.");
        }

        if (method == null) {
            throw new IllegalArgumentException("결제 수단이 필요합니다.");
        }

        Payment payment = new Payment(bill, amount, method);

        bill.setPaidAmount(bill.getPaidAmount() + amount);
        bill.setRemainingAmount(bill.getTotalAmount() - bill.getPaidAmount());

        if (bill.getRemainingAmount() == 0) {
            bill.setStatus(BillingStatus.PAID);
        } else {
            bill.setStatus(BillingStatus.CONFIRMED);
        }

        Payment saved = paymentRepository.save(payment);

        return new PaymentResponse(
                saved.getId(),
                saved.getBill().getId(),
                saved.getPaymentAmount(),
                saved.getStatus().name(),
                saved.getMethod().name(),
                saved.getPaidAt()
        );
    }

    /**
     * 수납 취소 (전체 취소)
     */
    public void cancelPayment(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(paymentId));

        // 이미 취소된 결제 방지
        if (payment.getStatus() == PaymentStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 결제입니다.");
        }

        // 완료된 결제만 취소 허용
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("취소 가능한 결제 상태가 아닙니다.");
        }

        Bill bill = payment.getBill();


        // 현재 구조에서는 환불이 어느 원결제에 연결되는지 추적하지 못하므로
        // 같은 청구에 REFUNDED 이력이 하나라도 있으면 전체 취소를 막는다.
        boolean hasRefundHistory =
                paymentRepository.countByBillIdAndStatus(bill.getId(), PaymentStatus.REFUNDED) > 0;

        if (hasRefundHistory) {
            throw new IllegalStateException("부분 환불 이력이 있는 청구는 전체 수납 취소를 할 수 없습니다.");
        }

        payment.cancel();

        bill.setPaidAmount(bill.getPaidAmount() - payment.getPaymentAmount());
        bill.setRemainingAmount(bill.getTotalAmount() - bill.getPaidAmount());

        if (bill.getPaidAmount() == 0) {
            bill.setStatus(BillingStatus.READY);
        } else {
            bill.setStatus(BillingStatus.CONFIRMED);
        }
    }

    /**
     * 부분 환불 기능
     */
    public PaymentResponse refundPayment(Long paymentId, Integer refundAmount) {

        Payment originalPayment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(paymentId));

        if (originalPayment.getStatus() != PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStatusException("환불 가능한 결제가 아닙니다.");
        }

        if (refundAmount == null || refundAmount <= 0) {
            throw new InvalidRefundAmountException("환불 금액은 0보다 커야 합니다.");
        }

        if (refundAmount > originalPayment.getPaymentAmount()) {
            throw new InvalidRefundAmountException("환불 금액이 결제 금액보다 클 수 없습니다.");
        }

        Bill bill = originalPayment.getBill();

        bill.setPaidAmount(bill.getPaidAmount() - refundAmount);
        bill.setRemainingAmount(bill.getTotalAmount() - bill.getPaidAmount());

        if (bill.getPaidAmount() == 0) {
            bill.setStatus(BillingStatus.READY);
        } else {
            bill.setStatus(BillingStatus.CONFIRMED);
        }

        Payment refund = new Payment(bill, refundAmount, originalPayment.getMethod());
        refund.setStatus(PaymentStatus.REFUNDED);

        Payment saved = paymentRepository.save(refund);

        return new PaymentResponse(
                saved.getId(),
                saved.getBill().getId(),
                saved.getPaymentAmount(),
                saved.getStatus().name(),
                saved.getMethod().name(),
                saved.getPaidAt()
        );
    }

    /**
     * 전체 결제 조회
     */
    public List<PaymentResponse> getPaymentsAsResponse() {
        return paymentRepository.findAll().stream()
                .map(p -> new PaymentResponse(
                        p.getId(),
                        p.getBill().getId(),
                        p.getPaymentAmount(),
                        p.getStatus().name(),
                        p.getMethod().name(),
                        p.getPaidAt()
                ))
                .toList();
    }

    /**
     * 청구 기준 결제 내역 조회
     */
    public List<PaymentResponse> getPaymentsByBill(Long billId) {

        return paymentRepository
                .findByBill_IdOrderByPaidAtDesc(billId)
                .stream()
                .map(p -> new PaymentResponse(
                        p.getId(),
                        p.getBill().getId(),
                        p.getPaymentAmount(),
                        p.getStatus().name(),
                        p.getMethod().name(),
                        p.getPaidAt()
                ))
                .toList();
    }
}