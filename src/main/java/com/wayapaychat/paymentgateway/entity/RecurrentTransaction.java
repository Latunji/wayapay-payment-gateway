package com.wayapaychat.paymentgateway.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.wayapaychat.paymentgateway.common.enums.Interval;
import com.wayapaychat.paymentgateway.common.enums.PaymentLinkType;
import com.wayapaychat.paymentgateway.common.enums.RecurrentPaymentStatus;
import com.wayapaychat.paymentgateway.entity.listener.FraudEventEntityListener;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EntityListeners(value = FraudEventEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "m_recurrent_transaction")
public class RecurrentTransaction extends GenericBaseEntity {
    @Column(name = "first_payment_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime firstPaymentDate;

    @Column(name = "next_charge_date_after_first_payment")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime nextChargeDateAfterFirstPayment;

    @Column(name = "next_charge_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime nextChargeDate;

    @Column(name = "last_charge_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime lastChargeDate;

    //paymentLinkId and customerId can be used to get the latest customer recurrent transaction on PaymentGateway
    @Column(name = "payment_link_id")
    private String paymentLinkId;

    @Column(name = "customer_subscription_id")
    private String customerSubscriptionId;

    @Column(name = "plan_id")
    private String planId;

    @Column(name = "plan_name")
    private String planName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "recurrent_transaction_id")
    private String recurrentTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_link_type")
    private PaymentLinkType paymentLinkType;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "current_transaction_ref_no")
    private String currentTransactionRefNo;

    @JsonIgnore
    @Column(name = "card_token_reference")
    private String cardTokenReference;

    @Column(name = "max_charge_count")
    private Integer maxChargeCount;

    @Column(name = "total_charge_count")
    private Integer totalChargeCount;

    @Column(name = "amount", nullable = false)
    private BigDecimal recurrentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "interval_type")
    private Interval intervalType;

    @Column(nullable = false, name = "interval")
    private Integer interval;

    @Column(name = "apply_date_after_first_payment")
    private Boolean applyDateAfterFirstPayment = false;

    @Column(nullable = false, name = "active")
    private Boolean active = true;

    //Unified Payment SessionId to charge a customer again
    @JsonIgnore
    @Column(name = "up_session_id")
    private String upSessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RecurrentPaymentStatus status;

    @PrePersist
    void prePersist() {
        if (ObjectUtils.isEmpty(active))
            this.active = false;
        if (ObjectUtils.isEmpty(status))
            status = RecurrentPaymentStatus.PENDING;
        if (ObjectUtils.isEmpty(totalChargeCount))
            this.totalChargeCount = 0;
        if (ObjectUtils.isEmpty(applyDateAfterFirstPayment))
            this.applyDateAfterFirstPayment = false;
        if (ObjectUtils.isEmpty(recurrentTransactionId))
            recurrentTransactionId = "RTX_" + RandomStringUtils.randomAlphanumeric(5)
                    + System.currentTimeMillis()
                    + RandomStringUtils.randomAlphanumeric(5);
    }
}
