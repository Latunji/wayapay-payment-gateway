package com.wayapaychat.paymentgateway.dao;

import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.pojo.waya.MerchantUnsettledSuccessfulTransaction;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.waya.SettlementQueryPojo;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionOverviewResponse;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionRevenueStats;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionYearMonthStats;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.TransactionReportStats;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.WayaWalletWithdrawal;
import com.wayapaychat.paymentgateway.service.impl.TransactionSettlementPojo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

public interface WayaPaymentDAO {

    List<TransactionReportStats> getTransactionRevenueStats();

    TransactionReportStats getTransactionReportStats(String merchantId, String mode);

    TransactionOverviewResponse getTransactionReport(String merchantId, String mode);

    @SuppressWarnings(value = "unchecked")
    List<TransactionYearMonthStats> getTransactionStatsByYearAndMonth(String merchantId, Long year, Date startDate, Date endDate, String mode);

    @SuppressWarnings(value = "unchecked")
    TransactionRevenueStats getTransactionGrossAndNetRevenue(String merchantId, String mode);

    @SuppressWarnings(value = "unchecked")
    List<MerchantUnsettledSuccessfulTransaction> merchantUnsettledSuccessTransactions(String merchantId);

    @SuppressWarnings(value = "unchecked")
    List<PaymentGateway> getAllTransactionsByRefNo(String delimiterRefNo);

    TransactionReportStats getWalletBalance(String merchantId, String mode);

    @SuppressWarnings(value = "unchecked")
    Page<TransactionSettlementPojo> getAllTransactionSettlement(SettlementQueryPojo settlementQueryPojo, String merchantId, Pageable pageable);

    // s-l done
    void expireAllTransactionMoreThan30Mins();
}
