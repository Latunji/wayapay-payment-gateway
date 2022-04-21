package com.wayapaychat.paymentgateway.pojo.waya;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantUnsettledSuccessfulTransaction {
    private String merchantId;
    private BigDecimal grossAmount;
    private BigDecimal netAmount;
    private BigDecimal totalFee;
}
