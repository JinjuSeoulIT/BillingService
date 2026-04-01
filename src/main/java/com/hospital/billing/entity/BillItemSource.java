package com.hospital.billing.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "BILL_ITEM_SOURCE")
public class BillItemSource {

    @Id
    // [추가] BILL_ITEM_SOURCE_INTG_SEQ 시퀀스 사용
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bill_item_source_seq_generator")
    @SequenceGenerator(
            name = "bill_item_source_seq_generator",
            sequenceName = "BILL_ITEM_SOURCE_INTG_SEQ",
            allocationSize = 1
    )
    @Column(name = "BILL_ITEM_SOURCE_ID")
    private Long id;

    // [추가] 어떤 BILL_ITEM의 근거인지 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BILL_ITEM_ID", nullable = false)
    private BillItem billItem;

    // [추가] 방문 기준
    @Column(name = "VISIT_ID")
    private Long visitId;

    // [추가] 근거 종류
    @Column(name = "SOURCE_TYPE", nullable = false, length = 30)
    private String sourceType;

    // [추가] 원본 데이터 PK
    @Column(name = "SOURCE_ID", nullable = false)
    private Long sourceId;

    // [추가] 원본 이벤트 ID
    @Column(name = "SOURCE_EVENT_ID", length = 100)
    private String sourceEventId;

    // [추가] 생성 시각
    @Column(name = "CREATED_AT", nullable = false)
    private Timestamp createdAt;

    protected BillItemSource() {
    }

    public BillItemSource(BillItem billItem,
                          Long visitId,
                          String sourceType,
                          Long sourceId,
                          String sourceEventId,
                          Timestamp createdAt) {
        this.billItem = billItem;
        this.visitId = visitId;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.sourceEventId = sourceEventId;
        this.createdAt = createdAt;
    }

    // [추가] 정적 팩토리 메서드
    public static BillItemSource create(BillItem billItem,
                                        Long visitId,
                                        String sourceType,
                                        Long sourceId,
                                        String sourceEventId,
                                        Timestamp createdAt) {
        return new BillItemSource(
                billItem,
                visitId,
                sourceType,
                sourceId,
                sourceEventId,
                createdAt
        );
    }

    public Long getId() {
        return id;
    }

    public BillItem getBillItem() {
        return billItem;
    }

    public void setBillItem(BillItem billItem) {
        this.billItem = billItem;
    }

    public Long getVisitId() {
        return visitId;
    }

    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceEventId() {
        return sourceEventId;
    }

    public void setSourceEventId(String sourceEventId) {
        this.sourceEventId = sourceEventId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}