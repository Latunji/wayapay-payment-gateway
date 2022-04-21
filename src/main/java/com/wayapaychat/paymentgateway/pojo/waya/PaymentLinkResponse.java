package com.wayapaychat.paymentgateway.pojo.waya;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.wayapaychat.paymentgateway.common.enums.Interval;
import com.wayapaychat.paymentgateway.common.enums.PaymentLinkType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLinkResponse {
    private String merchantId;
    private String paymentLinkId;
    private String successMessage;
    private PaymentLinkType paymentLinkType;
    private String paymentLinkName;
    private String description;
    private BigDecimal payableAmount;
    private String currency;
    private String amountText;
    private String customerPaymentLink;
    private String otherDetailsJSON;
    private String status;
    private String merchantKeyMode;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime expiryDate;
    private Integer interval;
    private Interval intervalType;
    private Integer totalCount;
    private String planId;
    private Boolean linkCanExpire;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startDateAfterFirstPayment;
}
