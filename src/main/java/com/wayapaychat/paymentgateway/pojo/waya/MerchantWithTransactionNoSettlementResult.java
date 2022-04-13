package com.wayapaychat.paymentgateway.pojo.waya;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantWithTransactionNoSettlementResult {
    private String merchantId;
    private BigDecimal totalUnsettledTransactionAmount;
}
