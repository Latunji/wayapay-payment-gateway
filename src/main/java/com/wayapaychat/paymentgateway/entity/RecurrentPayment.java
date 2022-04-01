package com.wayapaychat.paymentgateway.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.wayapaychat.paymentgateway.common.enums.Interval;
import com.wayapaychat.paymentgateway.common.enums.PaymentLinkType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

//@EntityListeners(value = FraudEventEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@Entity
//@Table(name = "m_recurrent_payment")
public class RecurrentPayment extends GenericBaseEntity {
    @Column(nullable = false, name = "first_payment_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime firstPaymentDate;

    @Column(nullable = false, name = "start_date_after_first_payment")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime startDateAfterFirstPayment;

    @Column(nullable = false, name = "next_charge_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime nextChargeDate;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime lastChargeDate;

    //paymentLinkId and customerId can be used to get the latest customer recurrent transaction on PaymentGateway
    @Column(name = "payment_link_id")
    private String paymentLinkId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_link_type")
    private PaymentLinkType paymentLinkType;

    @Column(name = "payment_link_id")
    private String customerId;

    @Column(name = "merchant_id")
    private String merchantId;

    @JsonIgnore
    @Column(nullable = false, name = "card_token_reference")
    private String cardTokenReference;

    @Column(nullable = false, name = "total_charge_count")
    private Long totalChargeCount;

    @Column(nullable = false, name = "current_charge_count")
    private Long currentChargeCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "interval")
    private Interval interval;

    @Column(nullable = false, name = "apply_date_after_first_payment")
    private Boolean applyDateAfterFirstPayment = false;

    @Column(nullable = false, name = "active")
    private Boolean active = true;
}
