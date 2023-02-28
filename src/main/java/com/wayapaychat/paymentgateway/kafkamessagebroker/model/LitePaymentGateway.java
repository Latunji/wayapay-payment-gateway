package com.wayapaychat.paymentgateway.kafkamessagebroker.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.wayapaychat.paymentgateway.common.enums.MerchantTransactionMode;
import com.wayapaychat.paymentgateway.enumm.SettlementStatus;
import com.wayapaychat.paymentgateway.enumm.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class LitePaymentGateway {
    private String refNo;
    private String tranId;
    private String merchantId;
    private String customerId;
    private TransactionStatus status;
    private MerchantTransactionMode mode;
    private SettlementStatus settlementStatus;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime settlementDate;
    private String settlementReferenceId;
    private Boolean transactionExpired;
    private String description;
    private BigDecimal amount;
    private BigDecimal fee;
    private String currencyCode;
    private String merchantName;
    private String settlementAccountNumber;
    private String settlementAccountName;
    private String settlementBankCode;
    private String settlementBankName;
}
