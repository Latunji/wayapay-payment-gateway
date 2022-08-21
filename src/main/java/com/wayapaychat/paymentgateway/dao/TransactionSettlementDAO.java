package com.wayapaychat.paymentgateway.dao;

import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionSettlementsResponse;

public interface TransactionSettlementDAO {
    TransactionSettlementsResponse merchantTransactionSettlementStats(String merchantId, String mode);

    String getLatestSettlementQuery(String merchantId, String mode);

    String getNextSettlementQuery(String merchantId, String mode);
}
