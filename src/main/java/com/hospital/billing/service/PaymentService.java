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
import com.hospital.billing.toss.client.TossPaymentClient;
import com.hospital.billing.toss.dto.TossCancelRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final TossPaymentClient tossPaymentClient;

    public PaymentService(PaymentRepository paymentRepository,
                          BillRepository billRepository,
                          TossPaymentClient tossPaymentClient) {
        this.paymentRepository = paymentRepository;
        this.billRepository = billRepository;
        this.tossPaymentClient = tossPaymentClient;
    }

    /**
     * 수납 생성 (기존 방식 유지)
     */
    public PaymentResponse createPayment(Long billId, Integer amount, PaymentMethod method) {
        return createPayment(billId, amount, method, null, null);
    }

    /**
     * 수납 생성 (토스 원거래 정보 포함)
     */
    public PaymentResponse createPayment(Long billId,
                                         Integer amount,
                                         PaymentMethod method,
                                         String paymentKey,
                                         String orderId) {

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

        Payment payment;

        // 카드 결제이면서 paymentKey / orderId 가 있으면 토스 원거래 정보까지 저장
        if (method == PaymentMethod.CARD
                && paymentKey != null && !paymentKey.isBlank()
                && orderId != null && !orderId.isBlank()) {

            payment = new Payment(bill, amount, method, paymentKey, orderId);

        } else {
            payment = new Payment(bill, amount, method);
        }

        int newPaidAmount = bill.getPaidAmount() + amount;
        applyBillAmounts(bill, newPaidAmount);

        Payment saved = paymentRepository.save(payment);

        return new PaymentResponse(
                saved.getId(),
                saved.getBill().getId(),
                saved.getPaymentAmount(),
                resolvePaymentStatus(saved),
                resolvePaymentMethod(saved),
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

        // 카드 결제면 토스 취소 먼저 호출
        if (payment.getMethod() == PaymentMethod.CARD) {
            if (payment.getPaymentKey() == null || payment.getPaymentKey().isBlank()) {
                throw new IllegalStateException("카드 결제의 paymentKey가 없어 토스 취소를 진행할 수 없습니다.");
            }

            TossCancelRequest cancelRequest = new TossCancelRequest();
            cancelRequest.setPaymentKey(payment.getPaymentKey());
            cancelRequest.setCancelReason("사용자 요청에 의한 전체 취소");
            cancelRequest.setCancelAmount(Long.valueOf(payment.getPaymentAmount()));

            tossPaymentClient.cancelPayment(cancelRequest);
        }

        int newPaidAmount = bill.getPaidAmount() - payment.getPaymentAmount();

        if (newPaidAmount < 0) {
            throw new IllegalStateException("취소 처리 후 결제 금액이 음수가 되어 취소를 진행할 수 없습니다.");
        }

        payment.cancel();
        applyBillAmounts(bill, newPaidAmount);
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

        // 1차 방어:
        // 현재 청구의 유효 결제 금액보다 더 많이 환불되면 안 됨
        if (refundAmount > bill.getPaidAmount()) {
            throw new InvalidRefundAmountException("환불 금액이 현재 유효 결제 금액보다 클 수 없습니다.");
        }

        int newPaidAmount = bill.getPaidAmount() - refundAmount;

        // 음수 방지
        if (newPaidAmount < 0) {
            throw new InvalidRefundAmountException("환불 처리 후 결제 금액이 음수가 될 수 없습니다.");
        }

        applyBillAmounts(bill, newPaidAmount);

        Payment refund = new Payment(bill, refundAmount, originalPayment.getMethod());
        refund.setStatus(PaymentStatus.REFUNDED);

        Payment saved = paymentRepository.save(refund);

        return new PaymentResponse(
                saved.getId(),
                saved.getBill().getId(),
                saved.getPaymentAmount(),
                resolvePaymentStatus(saved),
                resolvePaymentMethod(saved),
                saved.getPaidAt()
        );
    }

    /**
     * 전체 결제 조회
     */
    public List<PaymentResponse> getPaymentsAsResponse() {
        return paymentRepository.findAll().stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    /**
     * 청구 기준 결제 내역 조회
     */
    public List<PaymentResponse> getPaymentsByBill(Long billId) {

        return paymentRepository
                .findByBill_IdOrderByPaidAtDesc(billId)
                .stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBill().getId(),
                payment.getPaymentAmount(),
                resolvePaymentStatus(payment),
                resolvePaymentMethod(payment),
                payment.getPaidAt()
        );
    }

    private String resolvePaymentStatus(Payment payment) {
        return payment.getStatus() != null
                ? payment.getStatus().name()
                : "UNKNOWN";
    }

    private String resolvePaymentMethod(Payment payment) {
        return payment.getMethod() != null
                ? payment.getMethod().name()
                : "UNKNOWN";
    }

    /**
     * Bill 금액/상태 재계산 공통 처리
     */
    private void applyBillAmounts(Bill bill, int newPaidAmount) {
        if (newPaidAmount < 0) {
            throw new IllegalStateException("결제 금액은 음수가 될 수 없습니다.");
        }

        int totalAmount = bill.getTotalAmount();
        int newRemainingAmount = totalAmount - newPaidAmount;

        if (newRemainingAmount < 0) {
            throw new IllegalStateException("남은 금액은 음수가 될 수 없습니다.");
        }

        if (newRemainingAmount > totalAmount) {
            throw new IllegalStateException("남은 금액이 총 청구 금액보다 클 수 없습니다.");
        }

        bill.setPaidAmount(newPaidAmount);
        bill.setRemainingAmount(newRemainingAmount);

        if (newRemainingAmount == 0) {
            bill.setStatus(BillingStatus.PAID);
        } else if (newPaidAmount == 0) {
            bill.setStatus(BillingStatus.READY);
        } else {
            bill.setStatus(BillingStatus.CONFIRMED);
        }
    }
}