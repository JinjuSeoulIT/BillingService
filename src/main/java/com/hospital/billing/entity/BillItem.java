package com.hospital.billing.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "BILL_ITEM")
public class BillItem {

    @Id
    // [수정] Oracle DB + 실제 BILL_ITEM 시퀀스 구조에 맞게 변경
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bill_item_seq_generator")
    @SequenceGenerator(
            name = "bill_item_seq_generator",
            sequenceName = "BILL_ITEM_INTG_SEQ",
            allocationSize = 1
    )
    @Column(name = "BILL_ITEM_ID")
    private Long id;

    // 어떤 Bill에 속한 항목인지 이게 핵심
    @ManyToOne(fetch = FetchType.LAZY)
    // 실제 DB 컬럼명에 맞춤
    @JoinColumn(name = "BILL_ID", nullable = false)
    private Bill bill;

    // 진료 항목 이름 (예: 진찰료, X-ray 검사)
    // ITEM_NAME 길이를 200으로 확장
    @Column(name = "ITEM_NAME", nullable = false, length = 200)
    private String itemName;

    // [추가] 현업형 표시용 항목 분류
    @Column(name = "ITEM_CATEGORY", nullable = false, length = 30)
    private String itemCategory;

    // [추가] 수량
    @Column(name = "QUANTITY", nullable = false)
    private Integer quantity;

    // [추가] 단가
    @Column(name = "UNIT_PRICE", nullable = false)
    private Integer unitPrice;

    // 실제 청구 금액
    @Column(name = "ITEM_AMOUNT", nullable = false)
    private Integer amount;

    protected BillItem() {
    }

    // [추가] 기존 amount만 받던 구조는 하위 호환용으로 유지
    public BillItem(Bill bill, String itemName, Integer amount) {
        this(
                bill,
                itemName,
                "ETC",
                1,
                amount,
                amount
        );
    }

    // [추가] 확장된 생성자
    public BillItem(Bill bill,
                    String itemName,
                    String itemCategory,
                    Integer quantity,
                    Integer unitPrice,
                    Integer amount) {
        this.bill = bill;
        this.itemName = itemName;
        this.itemCategory = itemCategory;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = amount;
    }

    // 정적 팩토리 메서드 추가
    public static BillItem create(Bill bill, String itemName, Integer amount) {
        return new BillItem(bill, itemName, amount);
    }

    // [추가] 현업형 표시용 팩토리
    public static BillItem create(Bill bill,
                                  String itemName,
                                  String itemCategory,
                                  Integer quantity,
                                  Integer unitPrice,
                                  Integer amount) {
        return new BillItem(
                bill,
                itemName,
                itemCategory,
                quantity,
                unitPrice,
                amount
        );
    }

    // ===== getter / setter =====
    public Long getId() {
        return id;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Integer unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
