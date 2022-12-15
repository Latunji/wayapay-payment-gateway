package com.wayapaychat.paymentgateway.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.wayapaychat.paymentgateway.entity.listener.FraudEventEntityListener;
import com.wayapaychat.paymentgateway.enumm.AccountSettlementOption;
import com.wayapaychat.paymentgateway.enumm.SettlementStatus;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@EntityListeners(value = FraudEventEntityListener.class)
@Builder
@AllArgsConstructor
@Entity
@Table(name = "m_sandbox_transaction_settlement", indexes =
        {
                @Index(name = "ix_tbl_mts__col_sandbox_settlement_reference_id__uq", columnList = "settlement_reference_id", unique = true)
        }
)
public class SandboxTransactionSettlement extends GenericBaseEntity {
    @Column(name = "merchant_configured_settlement_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime merchantConfiguredSettlementDate;

    @Column(name = "date_settled")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dateSettled;

    @Column(name = "settlement_initiation_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime settlementInitiationDate;

    @Column(name = "date_modified")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dateModified;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String merchantId;

    @Column(name = "merchant_user_id")
    private Long merchantUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status")
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;

    @Column(name = "settlement_reference_id", columnDefinition = "VARCHAR(255)")
    private String settlementReferenceId;

    @Column(name = "settlement_net_amount")
    private BigDecimal settlementNetAmount = BigDecimal.ZERO;

    @Column(name = "settlement_gross_amount")
    private BigDecimal settlementGrossAmount = BigDecimal.ZERO;

    @Column(name = "total_fee")
    private BigDecimal totalFee = BigDecimal.ZERO;

    @Column(name = "settlement_account")
    private String settlementAccount;

    @Column(name = "account_settlement_option")
    @Enumerated(EnumType.STRING)
    private AccountSettlementOption accountSettlementOption;

    @Column(name = "settlement_beneficiary_account", columnDefinition = "VARCHAR(255)")
    private String settlementBeneficiaryAccount;

    @JsonIgnore
    @Column(name = "total_retry_settlement_count")
    private Long totalRetrySettlementCount;

    @PrePersist
    public void onCreate() {
        if (ObjectUtils.isEmpty(settlementReferenceId))
            settlementReferenceId = "SBSET_REF_" + RandomStringUtils.randomAlphanumeric(5)
                    + System.currentTimeMillis()
                    + RandomStringUtils.randomAlphanumeric(5);
        if (ObjectUtils.isEmpty(merchantConfiguredSettlementDate))
            merchantConfiguredSettlementDate = LocalDateTime.now().plusDays(2);
        if (ObjectUtils.isEmpty(accountSettlementOption))
            accountSettlementOption = AccountSettlementOption.WALLET;
        if (ObjectUtils.isEmpty(settlementBeneficiaryAccount))
            settlementBeneficiaryAccount = "Wayapay Wallet";
        if (ObjectUtils.isNotEmpty(this.getCreatedBy()))
            setCreatedBy(0L);
        if (ObjectUtils.isEmpty(totalRetrySettlementCount))
            totalRetrySettlementCount = 0L;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SandboxTransactionSettlement that = (SandboxTransactionSettlement) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
