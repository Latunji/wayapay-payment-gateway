package com.wayapaychat.paymentgateway.pojo.waya.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRevenueStats {
    private BigDecimal grossRevenue;
    private BigDecimal netRevenue;
}
