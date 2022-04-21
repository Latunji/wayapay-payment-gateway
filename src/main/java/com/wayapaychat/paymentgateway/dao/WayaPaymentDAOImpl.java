package com.wayapaychat.paymentgateway.dao;

import com.wayapaychat.paymentgateway.mapper.BigDecimalAmountWrapper;
import com.wayapaychat.paymentgateway.mapper.WalletRevenueMapper;
import com.wayapaychat.paymentgateway.pojo.waya.MerchantUnsettledSuccessfulTransaction;
import com.wayapaychat.paymentgateway.pojo.waya.stats.*;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.WalletRevenue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;

@Repository
@Slf4j
public class WayaPaymentDAOImpl implements WayaPaymentDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TransactionSettlementDAO transactionSettlementDAO;

    @Override
    public List<WalletRevenue> getRevenue() {
        List<WalletRevenue> product = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT merchant_id AS MERCHANTID, COUNT(a.TRAN_ID) AS TOTALTRAN,");
        query.append("SUM(CASE WHEN a.status = 'SUCCESSFUL' THEN 1 ELSE 0 END) as TOTALSUCCESS,");
        query.append("SUM(CASE WHEN a.status = 'FAILED' THEN 1 ELSE 0 END) as TOTALFAILED,");
        query.append("SUM(CASE WHEN a.status = 'ABANDONED' THEN 1 ELSE 0 END) as TOTALABANDONED,");
        query.append("SUM(CASE WHEN a.status = 'REFUNDED' THEN 1 ELSE 0 END) as TOTALREFUNDED,");
        query.append("SUM(CASE WHEN a.status = 'PENDING' THEN 1 ELSE 0 END) as TOTALPENDING,");
        query.append("SUM(CASE WHEN settled = 'SETTLED' THEN 1 ELSE 0 END) as TOTALSETTLED,");
        query.append("SUM(CASE WHEN settled = 'SETTLED' THEN tran_amount ELSE 0 END) as TOTALSETTLEDAMT,");
        query.append("SUM(CASE WHEN settled = 'NOT_SETTLED' THEN tran_amount ELSE 0 END) as TOTALUNSETTLEDAMT  ");
        query.append("FROM m_payment_gateway a LEFT JOIN m_payment_wallet b ON a.ref_no = b.ref_no  ");
        query.append("GROUP BY merchant_id ORDER BY TOTALTRAN DESC");
        String sql = query.toString();
        try {
            WalletRevenueMapper rowMapper = new WalletRevenueMapper();
            product = jdbcTemplate.query(sql, rowMapper);
            return product;
        } catch (Exception ex) {
            log.error("An error Occured: Cause: {} \r\n Message: {}", ex.getCause(), ex.getMessage());
            return null;
        }
    }

    @Override
    public WalletRevenue getRevenue(String merchantId) {
        WalletRevenue product = new WalletRevenue();
        StringBuilder query = new StringBuilder();
        query.append("SELECT merchant_id AS MERCHANTID, COUNT(a.TRAN_ID) AS TOTALTRAN,");
        query.append("SUM(CASE WHEN a.status = 'SUCCESSFUL' THEN 1 ELSE 0 END) as TOTALSUCCESS,");
        query.append("SUM(CASE WHEN a.status = 'FAILED' THEN 1 ELSE 0 END) as TOTALFAILED,");
        query.append("SUM(CASE WHEN a.status = 'ABANDONED' THEN 1 ELSE 0 END) as TOTALABANDONED,");
        query.append("SUM(CASE WHEN a.status = 'REFUNDED' THEN 1 ELSE 0 END) as TOTALREFUNDED,");
        query.append("SUM(CASE WHEN a.status = 'PENDING' THEN 1 ELSE 0 END) as TOTALPENDING,");
        query.append("SUM(CASE WHEN settled = 'SETTLED' THEN 1 ELSE 0 END) as TOTALSETTLED,");
        query.append("SUM(CASE WHEN settled = 'SETTLED' THEN tran_amount ELSE 0 END) as TOTALSETTLEDAMT,");
        query.append("SUM(CASE WHEN settled = 'NOT_SETTLED' THEN tran_amount ELSE 0 END) as TOTALUNSETTLEDAMT  ");
        query.append("FROM m_payment_gateway a LEFT JOIN m_payment_wallet b ON a.ref_no = b.ref_no where merchant_id = ?  ");
        query.append("GROUP BY merchant_id ORDER BY TOTALTRAN DESC");
        String sql = query.toString();
        WalletRevenueMapper rowMapper = new WalletRevenueMapper();
        Object[] params = new Object[]{merchantId};
        product = jdbcTemplate.queryForObject(sql, rowMapper, params);
        return product;
    }


    @Override
    @SuppressWarnings(value = "unchecked")
    public TransactionOverviewResponse getTransactionReport(final String merchantId) {
        @NotNull final String SUCCESS_RATE_TOTAL_SUB = " SELECT COUNT(*.status) total FROM m_payment_gateway WHERE UPPER(status) IN ('SUCCESSFUL','ERROR','FAILED') ";
        @NotNull final String REFUSAL_RATE_TOTAL_SUB = " SELECT COUNT(*.status) total FROM m_payment_gateway WHERE UPPER(status) " +
                "IN ('SYSTEM_ERROR','BANK_ERROR','CUSTOMER_ERROR', 'FRAUD_ERROR', 'FAILED') ";
        @NotNull final String TOTAL_PAYMENT_CHANNEL_SUB = " SELECT COUNT(*.channel) FROM m_payment_gateway WHERE UPPER(channel) " +
                "IN ('CARD','USSD','PAYATTITUDE', 'QR','BANK') ";
        @NotNull final String MER = String.format(" merchant_id = '%s' ", merchantId);
        @NotNull final String SUB_Q_MER = String.format(" WHERE %s ", MER);
        @NotNull final String SUB_Q_MER_AND = String.format(" AND %s ", MER);
        @NotNull final String SUB_Q_MER_GROUP = " GROUP BY merchant_id ";
        @NotNull final String TOTAL_TRANSACTION_SUB_Q_MER = String.format(" %s %s ", SUCCESS_RATE_TOTAL_SUB, SUB_Q_MER_AND);

        @NotNull final String TRANSACTION_STATUS_STATS_Q = String.format("SELECT COUNT(status), status FROM m_payment_gateway %s " +
                " GROUP BY status; ", SUB_Q_MER);

        @NotNull final String GROSS_REVENUE_Q = getGrossRevenueQuery(merchantId);

        @NotNull final String NET_REVENUE_Q = getNetRevenueQuery(merchantId);

        @NotNull final String YEAR_MONTH_STATS_Q = buildYearMonthQuery(merchantId, null);

        @NotNull final String LATEST_SETTLEMENT_Q = transactionSettlementDAO.getLatestSettlementQuery(merchantId);

        @NotNull final String NEXT_SETTLEMENT_Q = transactionSettlementDAO.getNextSettlementQuery(merchantId);

        @NotNull final String SUCCESS_ERROR_STATS_Q = String.format("SELECT COUNT(status), status " +
                " FROM m_payment_gateway WHERE status IN ('SUCCESSFUL','ERROR','FAILED') %s GROUP BY status; ", SUB_Q_MER_AND);

        @NotNull final String REFUSAL_ERROR_Q = String.format("SELECT  COUNT(status), status " +
                " FROM m_payment_gateway WHERE status IN ('SYSTEM_ERROR','BANK_ERROR','CUSTOMER_ERROR', 'FRAUD_ERROR', 'FAILED') %s GROUP BY status; ", SUB_Q_MER_AND);

        @NotNull final String PAYMENT_CHANNEL_STATS_Q = String.format("SELECT COUNT(channel), channel " +
                " FROM m_payment_gateway WHERE channel IN ('CARD','USSD','PAYATTITUDE', 'QR','BANK') %s GROUP BY channel ; ", SUB_Q_MER_AND);

        @NotNull final String TRANSACTION_REPORT_Q = TRANSACTION_STATUS_STATS_Q + GROSS_REVENUE_Q + NET_REVENUE_Q + YEAR_MONTH_STATS_Q + LATEST_SETTLEMENT_Q +
                NEXT_SETTLEMENT_Q + SUCCESS_ERROR_STATS_Q + REFUSAL_ERROR_Q + PAYMENT_CHANNEL_STATS_Q;

        var cscFactory = new CallableStatementCreatorFactory(TRANSACTION_REPORT_Q);
        var csc = cscFactory.newCallableStatementCreator(new HashMap<>());
        var returnedParams = List.<SqlParameter>of(
                new SqlReturnResultSet("status_stats", BeanPropertyRowMapper.newInstance(BigDecimalCountStatusWrapper.class)),
                new SqlReturnResultSet("gross_revenue", BeanPropertyRowMapper.newInstance(BigDecimalAmountWrapper.class)),
                new SqlReturnResultSet("net_revenue", BeanPropertyRowMapper.newInstance(BigDecimalAmountWrapper.class)),
                new SqlReturnResultSet("year_month_stats", BeanPropertyRowMapper.newInstance(TransactionYearMonthStats.class)),
                new SqlReturnResultSet("latest_settlement", BeanPropertyRowMapper.newInstance(BigDecimalAmountWrapper.class)),
                new SqlReturnResultSet("next_settlement", BeanPropertyRowMapper.newInstance(BigDecimalAmountWrapper.class)),
                new SqlReturnResultSet("success_error_stats", BeanPropertyRowMapper.newInstance(BigDecimalCountStatusWrapper.class)),
                new SqlReturnResultSet("refusal_error_stats", BeanPropertyRowMapper.newInstance(BigDecimalCountStatusWrapper.class)),
                new SqlReturnResultSet("payment_channel_stats", BeanPropertyRowMapper.newInstance(TransactionPaymentChannelStats.class)));

        Map<String, Object> results = jdbcTemplate.call(csc, returnedParams);
        TransactionOverviewResponse transactionOverviewResponse = TransactionOverviewResponse.builder().build();

        if (ObjectUtils.isNotEmpty(results)) {
            TransactionRevenueStats revenueStats = TransactionRevenueStats.builder().build();
            TransactionSettlementStats settlementStats = TransactionSettlementStats.builder().build();

            List<BigDecimalCountStatusWrapper> statusStats = (List<BigDecimalCountStatusWrapper>) results.get("status_stats");
            List<BigDecimalAmountWrapper> grossRevenue = (List<BigDecimalAmountWrapper>) results.get("gross_revenue");
            List<BigDecimalAmountWrapper> netRevenue = (List<BigDecimalAmountWrapper>) results.get("net_revenue");
            List<TransactionYearMonthStats> yearMonthStats = (List<TransactionYearMonthStats>) results.get("year_month_stats");
            List<BigDecimalAmountWrapper> latestSettlement = (List<BigDecimalAmountWrapper>) results.get("latest_settlement");
            List<BigDecimalAmountWrapper> nextSettlement = (List<BigDecimalAmountWrapper>) results.get("next_settlement");
            List<BigDecimalCountStatusWrapper> successErrorStats = (List<BigDecimalCountStatusWrapper>) results.get("success_error_stats");
            List<BigDecimalCountStatusWrapper> refusalErrorStats = (List<BigDecimalCountStatusWrapper>) results.get("refusal_error_stats");
            List<TransactionPaymentChannelStats> paymentChannelStats = (List<TransactionPaymentChannelStats>) results.get("payment_channel_stats");

            if (ObjectUtils.isNotEmpty(grossRevenue))
                revenueStats.setGrossRevenue(grossRevenue.get(0).getAmount());
            if (ObjectUtils.isNotEmpty(netRevenue))
                revenueStats.setNetRevenue(netRevenue.get(0).getAmount());
            transactionOverviewResponse.setRevenueStats(revenueStats);
            if (ObjectUtils.isNotEmpty(latestSettlement))
                settlementStats.setLatestSettlement(latestSettlement.get(0).getAmount());
            else settlementStats.setLatestSettlement(BigDecimal.ZERO);
            if (ObjectUtils.isNotEmpty(nextSettlement))
                settlementStats.setNextSettlement(nextSettlement.get(0).getAmount());
            else settlementStats.setNextSettlement(BigDecimal.ZERO);


            transactionOverviewResponse.setYearMonthStats(yearMonthStats);
            transactionOverviewResponse.setSettlementStats(settlementStats);
            transactionOverviewResponse.setStatusStats(statusStats);
            transactionOverviewResponse.setSuccessErrorStats(successErrorStats);
            transactionOverviewResponse.setRefusalErrorStats(refusalErrorStats);
            transactionOverviewResponse.setPaymentChannelStats(paymentChannelStats);

        }
        return transactionOverviewResponse;
    }


    @Override
    @SuppressWarnings(value = "unchecked")
    public List<TransactionYearMonthStats> getMerchantTransactionStatsByYearAndMonth(final String merchantId, final Long year) {
        @NotNull final String FINAL_Q = buildYearMonthQuery(merchantId, year);
        var cscFactory = new CallableStatementCreatorFactory(FINAL_Q);
        var csc = cscFactory.newCallableStatementCreator(new HashMap<>());
        var returnedParams = List.<SqlParameter>of(
                new SqlReturnResultSet("year_month_stats", BeanPropertyRowMapper.newInstance(TransactionYearMonthStats.class)));
        Map<String, Object> results = jdbcTemplate.call(csc, returnedParams);
        if (ObjectUtils.isNotEmpty(results))
            return (List<TransactionYearMonthStats>) results.get("year_month_stats");
        return List.of();
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public TransactionRevenueStats getMerchantTransactionGrossAndNetRevenue(final String merchantId) {
        @NotNull final String GROSS_REVENUE_Q = getGrossRevenueQuery(merchantId);
        @NotNull final String NET_REVENUE_Q = getNetRevenueQuery(merchantId);
        @NotNull final String FINAL_Q = GROSS_REVENUE_Q + NET_REVENUE_Q;
        var cscFactory = new CallableStatementCreatorFactory(FINAL_Q);
        var csc = cscFactory.newCallableStatementCreator(new HashMap<>());
        var returnedParams = List.<SqlParameter>of(
                new SqlReturnResultSet("gross_revenue", BeanPropertyRowMapper.newInstance(BigDecimalAmountWrapper.class)),
                new SqlReturnResultSet("net_revenue", BeanPropertyRowMapper.newInstance(BigDecimalAmountWrapper.class)));
        Map<String, Object> results = jdbcTemplate.call(csc, returnedParams);
        TransactionRevenueStats revenueStats = TransactionRevenueStats.builder().build();
        if (ObjectUtils.isNotEmpty(results)) {
            List<BigDecimalAmountWrapper> grossRevenue = (List<BigDecimalAmountWrapper>) results.get("gross_revenue");
            List<BigDecimalAmountWrapper> netRevenue = (List<BigDecimalAmountWrapper>) results.get("net_revenue");
            if (ObjectUtils.isNotEmpty(grossRevenue))
                revenueStats.setGrossRevenue(grossRevenue.get(0).getAmount());
            if (ObjectUtils.isNotEmpty(netRevenue))
                revenueStats.setNetRevenue(netRevenue.get(0).getAmount());
            return revenueStats;
        }
        return revenueStats;
    }

    private String buildYearMonthQuery(String merchantId, Long year) {
        final Long currentYear = ObjectUtils.isEmpty(year) ? Calendar.getInstance().get(Calendar.YEAR) : year;
        @NotNull final String ALT_Q = " merchant_id IS NOT NULL ";
        @NotNull final String MER = String.format(" merchant_id = '%s' ", merchantId);
        @NotNull final String EXTRACT_Q = " EXTRACT( year FROM tran_date ) ";
        @NotNull final String SUB_EXTRACT_Q = String.format(" EXTRACT( year FROM tran_date ) = %s ", currentYear);
        @NotNull final String SUB_Q_MER = ObjectUtils.isNotEmpty(merchantId) ? String.format(" %s ", MER) : String.format(" %s ", ALT_Q);
        @NotNull final String FINAL_Q = String.format(" SELECT %s as year , COUNT(*), TO_CHAR(tran_date, 'Mon') AS month, merchant_id FROM m_payment_gateway t " +
                " WHERE %s AND %s GROUP BY month,merchant_id,year ;", EXTRACT_Q, SUB_EXTRACT_Q, SUB_Q_MER);
        return FINAL_Q;
    }

    private String getGrossRevenueQuery(String merchantId) {
        @NotNull final String SUB_Q_MER_AND = String.format(" AND merchant_id = '%s' ", merchantId);
        return String.format("SELECT SUM(amount) amount FROM m_payment_gateway " +
                " WHERE status='SUCCESSFUL' %s ;", SUB_Q_MER_AND);
    }

    private String getNetRevenueQuery(String merchantId) {
        @NotNull final String SUB_Q_MER_AND = String.format(" AND merchant_id = '%s' ", merchantId);
        return String.format("SELECT ( SUM(amount) - SUM(fee) ) amount FROM m_payment_gateway " +
                " WHERE status='SUCCESSFUL' %s ;", SUB_Q_MER_AND);
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public List<MerchantUnsettledSuccessfulTransaction> merchantUnsettledSuccessTransactions(final String merchantId) {
        @NotNull final String SETTLEMENT_STATS_Q = getMerchantSettlementStatsQuery(merchantId);
        var cscFactory = new CallableStatementCreatorFactory(SETTLEMENT_STATS_Q);
        var csc = cscFactory.newCallableStatementCreator(new HashMap<>());
        var returnedParams = List.<SqlParameter>of(
                new SqlReturnResultSet("unsettled_transaction", BeanPropertyRowMapper.newInstance(MerchantUnsettledSuccessfulTransaction.class)));
        Map<String, Object> results = jdbcTemplate.call(csc, returnedParams);
        if (ObjectUtils.isNotEmpty(results))
            return (List<MerchantUnsettledSuccessfulTransaction>) results.get("unsettled_transaction");
        return List.of();
    }

    private String getMerchantSettlementStatsQuery(String merchantId) {
        @NotNull final String SUB_Q_MER_AND = ObjectUtils.isEmpty(merchantId) ? "" : String.format(" AND merchant_id = '%s' ", merchantId);
        return String.format("SELECT merchant_id, SUM(fee) as total_fee, SUM(amount) gross_amount, " +
                " (SUM(amount) - SUM(fee) ) net_amount FROM m_payment_gateway WHERE status = 'SUCCESSFUL' %s" +
                " AND settlement_status = 'PENDING' GROUP BY merchant_id ", SUB_Q_MER_AND);
    }
}
