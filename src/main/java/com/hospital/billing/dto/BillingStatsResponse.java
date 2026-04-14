package com.hospital.billing.dto;

public class BillingStatsResponse {

    private long readyCount;          // 결제 대기 건수
    private long confirmedCount;      // 부분 수납 건수
    private long paidCount;           // 결제 완료 건수
    private long finalConfirmedCount; // 완납 후 청구 확정 건수

    // ===== [CHANGED] 금액 통계 구조 변경 =====
    private long todayCompletedAmount;   // 오늘 결제(COMPLETED) 합계
    private long todayRefundedAmount;    // 오늘 환불(REFUNDED) 합계
    private long totalCompletedAmount;   // 전체 결제(COMPLETED) 합계
    private long totalRefundedAmount;    // 전체 환불(REFUNDED) 합계

    private long todayNetAmount;         // 오늘 순수납 (completed - refunded)
    private long totalNetAmount;         // 전체 순수납 (completed - refunded)

    public BillingStatsResponse(long readyCount,
                                long confirmedCount,
                                long paidCount,
                                long finalConfirmedCount,

                                // [CHANGED] 생성자 파라미터 변경
                                long todayCompletedAmount,
                                long todayRefundedAmount,
                                long totalCompletedAmount,
                                long totalRefundedAmount,
                                long todayNetAmount,
                                long totalNetAmount)
    {
        this.readyCount = readyCount;
        this.confirmedCount = confirmedCount;
        this.paidCount = paidCount;
        this.finalConfirmedCount = finalConfirmedCount;

        // [CHANGED] 필드 세팅 변경
        this.todayCompletedAmount = todayCompletedAmount;
        this.todayRefundedAmount = todayRefundedAmount;
        this.totalCompletedAmount = totalCompletedAmount;
        this.totalRefundedAmount = totalRefundedAmount;
        this.todayNetAmount = todayNetAmount;
        this.totalNetAmount = totalNetAmount;
    }

    public long getReadyCount() {
        return readyCount;
    }

    public long getConfirmedCount() {
        return confirmedCount;
    }

    public long getPaidCount() {
        return paidCount;
    }

    public long getFinalConfirmedCount() {
        return finalConfirmedCount;
    }

    // ===== [CHANGED] 새 getter 추가 =====
    public long getTodayCompletedAmount() {
        return todayCompletedAmount;
    }

    public long getTodayRefundedAmount() {
        return todayRefundedAmount;
    }

    public long getTotalCompletedAmount() {
        return totalCompletedAmount;
    }

    public long getTotalRefundedAmount() {
        return totalRefundedAmount;
    }

    public long getTodayNetAmount() {
        return todayNetAmount;
    }

    public long getTotalNetAmount() {
        return totalNetAmount;
    }
}