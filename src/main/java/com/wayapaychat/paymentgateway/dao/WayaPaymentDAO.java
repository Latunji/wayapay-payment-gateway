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

public interface WayaPaymentDAO {

    List<TransactionReportStats> getTransactionReportStats();

    TransactionReportStats getTransactionReportStats(String merchantId, String mode);

    TransactionOverviewResponse getTransactionReport(String merchantId, String mode);

    @SuppressWarnings(value = "unchecked")
    List<TransactionYearMonthStats> getMerchantTransactionStatsByYearAndMonth(String merchantId, Long year, Date startDate, Date endDate, String mode);

    @SuppressWarnings(value = "unchecked")
    TransactionRevenueStats getMerchantTransactionGrossAndNetRevenue(String merchantId, String mode);

    @SuppressWarnings(value = "unchecked")
    List<MerchantUnsettledSuccessfulTransaction> merchantUnsettledSuccessTransactions(String merchantId);

    @SuppressWarnings(value = "unchecked")
    List<PaymentGateway> getAllTransactionsByRefNo(String delimiterRefNo);

    @SuppressWarnings(value = "unchecked")
    Page<TransactionSettlementPojo> getAllTransactionSettlement(SettlementQueryPojo settlementQueryPojo, String merchantId, Pageable pageable);

    // s-l done
    Boolean expireAllTransactionMoreThan30Mins();
}
