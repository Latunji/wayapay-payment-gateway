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
public class TransactionSettlementsResponse {
    private TransactionSettlementStats stats;
    private List<BigDecimalCountStatusWrapper> counts;
}
