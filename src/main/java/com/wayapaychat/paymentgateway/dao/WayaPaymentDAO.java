package com.wayapaychat.paymentgateway.dao;

import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.pojo.waya.MerchantUnsettledSuccessfulTransaction;
import com.wayapaychat.paymentgateway.pojo.waya.SettlementQueryPojo;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionOverviewResponse;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionRevenueStats;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionYearMonthStats;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.TransactionReportStats;
import com.wayapaychat.paymentgateway.service.impl.TransactionSettlementPojo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.*;

public interface WayaPaymentDAO {

    List<TransactionReportStats> getTransactionReportStats();

    TransactionReportStats getTransactionReportStats(String merchantId);

    TransactionOverviewResponse getTransactionReport(String merchantId);

    @SuppressWarnings(value = "unchecked")
    List<TransactionYearMonthStats> getMerchantTransactionStatsByYearAndMonth(String merchantId, Long year, Date startDate, Date endDate);

    @SuppressWarnings(value = "unchecked")
    Object getMerchantTransactionGrossAndNetRevenue(String merchantId);

    @SuppressWarnings(value = "unchecked")
    List<MerchantUnsettledSuccessfulTransaction> merchantUnsettledSuccessTransactions(String merchantId);

    @SuppressWarnings(value = "unchecked")
    List<PaymentGateway> getAllTransactionsByRefNo(String delimiterRefNo);

    @SuppressWarnings(value = "unchecked")
    Page<TransactionSettlementPojo> getAllTransactionSettlement(SettlementQueryPojo settlementQueryPojo,String merchantId, Pageable pageable);

    void expireAllTransactionLessThan30Mins();
}
