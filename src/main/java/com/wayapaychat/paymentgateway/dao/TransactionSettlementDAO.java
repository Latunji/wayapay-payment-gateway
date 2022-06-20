package com.wayapaychat.paymentgateway.dao;

import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionSettlementsResponse;

public interface TransactionSettlementDAO {
    TransactionSettlementsResponse merchantTransactionSettlementStats(String merchantId);

    String getLatestSettlementQuery(String merchantId);

    String getNextSettlementQuery(String merchantId);
}