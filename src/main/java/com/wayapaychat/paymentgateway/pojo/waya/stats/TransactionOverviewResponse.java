package com.wayapaychat.paymentgateway.pojo.waya.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionOverviewResponse {
    private List<BigDecimalCountStatusWrapper> statusStats;
    private TransactionRevenueStats revenueStats;
    private List<TransactionYearMonthStats> yearMonthStats;
    private TransactionSettlementStats settlementStats;
    private List<BigDecimalCountStatusWrapper> successErrorStats;
    private List<BigDecimalCountStatusWrapper> refusalErrorStats;
    private List<TransactionPaymentChannelStats> paymentChannelStats;
}

