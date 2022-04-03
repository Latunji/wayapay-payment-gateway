package com.wayapaychat.paymentgateway.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.wayapaychat.paymentgateway.common.enums.Interval;
import com.wayapaychat.paymentgateway.common.enums.PaymentLinkType;
import com.wayapaychat.paymentgateway.entity.listener.FraudEventEntityListener;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@EntityListeners(value = FraudEventEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "m_recurrent_payment")
public class RecurrentPayment extends GenericBaseEntity {
    @Column(name = "first_payment_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime firstPaymentDate;

    @Column(name = "start_date_after_first_payment")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime startDateAfterFirstPayment;

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

    @Column(name = "plan_id")
    private String planId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_link_type")
    private PaymentLinkType paymentLinkType;

    @Column(name = "payment_link_id")
    private String customerId;

    @Column(name = "merchant_id")
    private String merchantId;

    @JsonIgnore
    @Column(name = "card_token_reference")
    private String cardTokenReference;

    @Column(name = "total_count")
    private Integer totalCount;

    @Column(name = "amount", nullable = false)
    private BigDecimal recurrentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "interval_type")
    private Interval intervalType;

    @Enumerated(EnumType.STRING)
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
}
