package com.hospital.billing.service;

import com.hospital.billing.dto.BillingStatsResponse;
import com.hospital.billing.entity.BillingStatus;
import com.hospital.billing.entity.PaymentStatus;
import com.hospital.billing.repository.BillRepository;
import com.hospital.billing.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class BillingStatsQueryService {

    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;

    public BillingStatsQueryService(BillRepository billRepository,
                                    PaymentRepository paymentRepository) {
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
    }

    public BillingStatsResponse getStats() {

        // 청구 상태 집계
        long readyCount =
                billRepository.countByStatus(BillingStatus.READY);

        long confirmedCount =
                billRepository.countByStatus(BillingStatus.CONFIRMED);

        long paidCount =
                billRepository.countByStatus(BillingStatus.PAID);

        // 오늘 범위 계산
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        Timestamp start = Timestamp.valueOf(startOfDay);
        Timestamp end = Timestamp.valueOf(endOfDay);

        // 오늘 COMPLETED 합
        long todayCompleted =
                paymentRepository.sumTodayByStatus(
                        PaymentStatus.COMPLETED,
                        start,
                        end
                );

        // 오늘 REFUNDED 합
        long todayRefunded =
                paymentRepository.sumTodayByStatus(
                        PaymentStatus.REFUNDED,
                        start,
                        end
                );

        //  순수납 별도 계산 
        long todayNet = todayCompleted - todayRefunded;

        // 전체 COMPLETED 합
        long totalCompleted =
                paymentRepository.sumByStatus(
                        PaymentStatus.COMPLETED
                );

        // 전체 REFUNDED 합
        long totalRefunded =
                paymentRepository.sumByStatus(
                        PaymentStatus.REFUNDED
                );

        // 순수납 별도 계산
        long totalNet = totalCompleted - totalRefunded;

        // 생성자 파라미터 변경
        return new BillingStatsResponse(
                readyCount,
                confirmedCount,
                paidCount,

                todayCompleted,     // 오늘 결제
                todayRefunded,      // 오늘 환불
                totalCompleted,     // 전체 결제
                totalRefunded,      // 전체 환불
                todayNet,           // 오늘 순수납
                totalNet            // 전체 순수납
        );
    }
}