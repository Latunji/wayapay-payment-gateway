package com.wayapaychat.paymentgateway.pojo.waya.stats;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSettlementsResponse {
    private TransactionSettlementStats stats;
    private SettledTransactionStats counts;
}
