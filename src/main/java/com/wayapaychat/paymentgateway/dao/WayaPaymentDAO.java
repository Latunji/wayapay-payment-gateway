package com.wayapaychat.paymentgateway.dao;

import com.wayapaychat.paymentgateway.pojo.waya.MerchantUnsettledSuccessfulTransaction;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionOverviewResponse;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionRevenueStats;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionYearMonthStats;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.WalletRevenue;
import com.wayapaychat.paymentgateway.service.impl.TransactionSettlementPojo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

public interface WayaPaymentDAO {

    List<WalletRevenue> getRevenue();

    WalletRevenue getRevenue(String merchantId);

    TransactionOverviewResponse getTransactionReport(String merchantId);

    @SuppressWarnings(value = "unchecked")
    List<TransactionYearMonthStats> getMerchantTransactionStatsByYearAndMonth(String merchantId, Long year, Date startDate, Date endDate);

    @SuppressWarnings(value = "unchecked")
    TransactionRevenueStats getMerchantTransactionGrossAndNetRevenue(String merchantId);

    @SuppressWarnings(value = "unchecked")
    List<MerchantUnsettledSuccessfulTransaction> merchantUnsettledSuccessTransactions(String merchantId);

    Page<TransactionSettlementPojo> getAllTransactionSettlement(String merchantIdToUse, Pageable pageable);
}
