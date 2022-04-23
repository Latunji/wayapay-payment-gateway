package com.wayapaychat.paymentgateway.service.impl;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.wayapaychat.paymentgateway.enumm.AccountSettlementOption;
import com.wayapaychat.paymentgateway.enumm.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSettlementPojo {
    private SettlementStatus settlementStatus;
    private BigDecimal settlementNetAmount;
    private BigDecimal settlementGrossAmount;
    private BigDecimal fee;

    private String merchantId;
    private String settlementReferenceId;
    private String settlementAccount;
    private AccountSettlementOption accountSettlementOption;
    private String settlementBeneficiaryAccount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime settlementDate;
}
