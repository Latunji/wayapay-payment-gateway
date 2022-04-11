package com.wayapaychat.paymentgateway.dao;

import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionOverviewResponse;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionRevenueStats;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionYearMonthStats;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.WalletRevenue;

import java.util.List;

public interface WayaPaymentDAO {

    List<WalletRevenue> getRevenue();

    WalletRevenue getRevenue(String merchantId);

    TransactionOverviewResponse getTransactionReport(String merchantId);

    @SuppressWarnings(value = "unchecked")
    List<TransactionYearMonthStats> getMerchantTransactionStatsByYearAndMonth(String merchantId, Long year);

    @SuppressWarnings(value = "unchecked")
    TransactionRevenueStats getMerchantTransactionGrossAndNetRevenue(String merchantId);
}
