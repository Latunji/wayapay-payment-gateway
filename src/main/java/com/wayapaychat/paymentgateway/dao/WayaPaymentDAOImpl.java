package com.wayapaychat.paymentgateway.dao;

import com.wayapaychat.paymentgateway.common.enums.MerchantTransactionMode;
import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.exception.ApplicationException;
import com.wayapaychat.paymentgateway.mapper.BigDecimalAmountWrapper;
import com.wayapaychat.paymentgateway.mapper.SettlementWrapper;
import com.wayapaychat.paymentgateway.mapper.WalletRevenueMapper;
import com.wayapaychat.paymentgateway.pojo.waya.MerchantUnsettledSuccessfulTransaction;
import com.wayapaychat.paymentgateway.pojo.waya.SettlementQueryPojo;
import com.wayapaychat.paymentgateway.pojo.waya.stats.*;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.TransactionReportStats;
import com.wayapaychat.paymentgateway.service.impl.TransactionSettlementPojo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Repository
@Slf4j
public class WayaPaymentDAOImpl implements WayaPaymentDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TransactionSettlementDAO transactionSettlementDAO;

    @Override
    public List<TransactionReportStats> getTransactionRevenueStats() {
        List<TransactionReportStats> product;
        StringBuilder query = new StringBuilder();
        query.append("SELECT COUNT(a.TRAN_ID) AS TOTALTRAN,");
        query.append("SUM(CASE WHEN a.status = 'SUCCESSFUL' THEN 1 ELSE 0 END) as TOTALSUCCESS,");
        query.append("SUM(CASE WHEN a.status = 'FAILED'     THEN 1 ELSE 0 END) as TOTALFAILED,");
        query.append("SUM(CASE WHEN a.status = 'ABANDONED'  THEN 1 ELSE 0 END) as TOTALABANDONED,");
        query.append("SUM(CASE WHEN a.status = 'REFUNDED'   THEN 1 ELSE 0 END) as TOTALREFUNDED,");
        query.append("SUM(CASE WHEN a.status = 'PENDING'    THEN 1 ELSE 0 END) as TOTALPENDING,");
        query.append("SUM(CASE WHEN a.status = 'DECLINED'   THEN 1 ELSE 0 END) as TOTALDECLINED,");
        query.append("SUM(CASE WHEN settlement_status =  'SETTLED'    THEN 1 ELSE 0 END) as TOTALSETTLED ");
        query.append("FROM m_payment_gateway a ");
        String sql = query.toString();
        WalletRevenueMapper rowMapper = new WalletRevenueMapper();
        product = jdbcTemplate.query(sql, rowMapper);
        return product;
    }

    // s-l done
    @Override
    public TransactionReportStats getTransactionReportStats(String merchantId, String mode) {
        String tbl;
        TransactionReportStats product;
        if(mode.equals(MerchantTransactionMode.TEST.toString())){
            tbl = "m_sandbox_payment_gateway";
        } else {
            tbl = "m_payment_gateway";
        }
        StringBuilder query = new StringBuilder();
        query.append("SELECT COUNT(a.TRAN_ID) AS TOTALTRAN,");
        query.append("SUM(CASE WHEN a.status = 'SUCCESSFUL' THEN 1 ELSE 0 END) as TOTALSUCCESS,");
        query.append("SUM(CASE WHEN a.status = 'FAILED'     THEN 1 ELSE 0 END) as TOTALFAILED,");
        query.append("SUM(CASE WHEN a.status = 'ABANDONED'  THEN 1 ELSE 0 END) as TOTALABANDONED,");
        query.append("SUM(CASE WHEN a.status = 'REFUNDED'   THEN 1 ELSE 0 END) as TOTALREFUNDED,");
        query.append("SUM(CASE WHEN a.status = 'PENDING'    THEN 1 ELSE 0 END) as TOTALPENDING,");
        query.append("SUM(CASE WHEN a.status = 'DECLINED'   THEN 1 ELSE 0 END) as TOTALDECLINED,");
        query.append("SUM(CASE WHEN settlement_status =  'SETTLED'    THEN 1 ELSE 0 END) as TOTALSETTLED ");
        query.append(String.format(" FROM %s a WHERE merchant_id = '%s' ", tbl, merchantId));
        String sql = query.toString();
        WalletRevenueMapper rowMapper = new WalletRevenueMapper();
        product = jdbcTemplate.queryForObject(sql, rowMapper);
        return product;
    }

    // s-l done
    @Override
    @SuppressWarnings(value = "unchecked")
    public TransactionOverviewResponse getTransactionReport(String merchantId, String mode) {
        String tbl;
        if(mode.equals(MerchantTransactionMode.TEST.toString())){
            tbl = "m_sandbox_payment_gateway";
        } else {
            tbl = "m_payment_gateway";
        }
        @NotNull final String SUCCESS_RATE_TOTAL_SUB = String.format(" SELECT COUNT(*.status) total FROM %s WHERE UPPER(status) IN ('SUCCESSFUL','ERROR','FAILED') ", tbl);
        @NotNull final String REFUSAL_RATE_TOTAL_SUB = String.format(" SELECT COUNT(*.status) total FROM %s WHERE UPPER(status) IN ('SYSTEM_ERROR','BANK_ERROR','CUSTOMER_ERROR', 'FRAUD_ERROR', 'FAILED') ", tbl);
        @NotNull final String TOTAL_PAYMENT_CHANNEL_SUB = String.format(" SELECT COUNT(*.channel) FROM %s WHERE UPPER(channel) IN ('CARD','USSD','PAYATTITUDE', 'QR','BANK') ", tbl);
        @NotNull final String MER = ObjectUtils.isEmpty(merchantId) ? "" : String.format(" merchant_id = '%s' ", merchantId);
        @NotNull final String SUB_Q_MER = ObjectUtils.isEmpty(merchantId) ? "" : String.format(" WHERE %s ", MER);
        @NotNull final String SUB_Q_MER_AND = ObjectUtils.isEmpty(merchantId) ? "" : String.format(" AND %s ", MER);
        @NotNull final String SUB_Q_MER_GROUP = " GROUP BY merchant_id ";
        @NotNull final String TOTAL_TRANSACTION_SUB_Q_MER = String.format(" %s %s ", SUCCESS_RATE_TOTAL_SUB, SUB_Q_MER_AND);

        @NotNull final String TRANSACTION_STATUS_STATS_Q = String.format("SELECT COUNT(status), status FROM %s %s  GROUP BY status; ", tbl, ObjectUtils.isEmpty(merchantId) ? " " : SUB_Q_MER);

        @NotNull final String GROSS_REVENUE_Q = getGrossRevenueQuery(merchantId, mode);

        @NotNull final String NET_REVENUE_Q = getNetRevenueQuery(merchantId, mode);

        @NotNull final String YEAR_MONTH_STATS_Q = buildYearMonthQuery(merchantId, null, null, null, mode);

        @NotNull final String LATEST_SETTLEMENT_Q = transactionSettlementDAO.getLatestSettlementQuery(merchantId, mode);

        @NotNull final String NEXT_SETTLEMENT_Q = transactionSettlementDAO.getNextSettlementQuery(merchantId, mode);

        @NotNull final String SUCCESS_ERROR_STATS_Q = String.format("SELECT COUNT(status), status " +
                        " FROM %s WHERE status IN ('SUCCESSFUL','ERROR','FAILED') %s GROUP BY status; ", tbl, ObjectUtils.isEmpty(merchantId) ? " " : SUB_Q_MER_AND);

        @NotNull final String REFUSAL_ERROR_Q = String.format("SELECT  COUNT(status), status " +
                        " FROM %s WHERE status IN ('SYSTEM_ERROR','BANK_ERROR','CUSTOMER_ERROR', 'FRAUD_ERROR', 'FAILED') %s GROUP BY status; ", tbl, ObjectUtils.isEmpty(merchantId) ? " " : SUB_Q_MER_AND);

        @NotNull final String PAYMENT_CHANNEL_STATS_Q = String.format("SELECT COUNT(channel), channel " +
                        " FROM %s WHERE channel IN ('CARD','USSD','PAYATTITUDE','QR','BANK') %s GROUP BY channel ; ", tbl, ObjectUtils.isEmpty(merchantId) ? " " : SUB_Q_MER_AND);

        @NotNull final String TRANSACTION_REPORT_Q = TRANSACTION_STATUS_STATS_Q + GROSS_REVENUE_Q + NET_REVENUE_Q + YEAR_MONTH_STATS_Q + LATEST_SETTLEMENT_Q +
                NEXT_SETTLEMENT_Q + SUCCESS_ERROR_STATS_Q + REFUSAL_ERROR_Q + PAYMENT_CHANNEL_STATS_Q;

        var cscFactory = new CallableStatementCreatorFactory(TRANSACTION_REPORT_Q);
        var csc = cscFactory.newCallableStatementCreator(new HashMap<>());
        var returnedParams = List.<SqlParameter>of(
                new SqlReturnResultSet("status_stats", BeanPropertyRowMapper.newInstance(BigDecimalCountStatusWrapper.class)),
                new SqlReturnResultSet("gross_revenue", BeanPropertyRowMapper.newInstance(BigDecimalAmountWrapper.class)),
                new SqlReturnResultSet("net_revenue", BeanPropertyRowMapper.newInstance(BigDecimalAmountWrapper.class)),
                new SqlReturnResultSet("year_month_stats", BeanPropertyRowMapper.newInstance(TransactionYearMonthStats.class)),
                new SqlReturnResultSet("latest_settlement", BeanPropertyRowMapper.newInstance(SettlementWrapper.class)),
                new SqlReturnResultSet("next_settlement", BeanPropertyRowMapper.newInstance(SettlementWrapper.class)),
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
            List<SettlementWrapper> latestSettlement = (List<SettlementWrapper>) results.get("latest_settlement");
            List<SettlementWrapper> nextSettlement = (List<SettlementWrapper>) results.get("next_settlement");
            List<BigDecimalCountStatusWrapper> successErrorStats = (List<BigDecimalCountStatusWrapper>) results.get("success_error_stats");
            List<BigDecimalCountStatusWrapper> refusalErrorStats = (List<BigDecimalCountStatusWrapper>) results.get("refusal_error_stats");
            List<TransactionPaymentChannelStats> paymentChannelStats = (List<TransactionPaymentChannelStats>) results.get("payment_channel_stats");

            if (ObjectUtils.isNotEmpty(grossRevenue.get(0)) && ObjectUtils.isNotEmpty(grossRevenue.get(0).getAmount())){
                revenueStats.setGrossRevenue(grossRevenue.get(0).getAmount());
            } else revenueStats.setGrossRevenue(BigDecimal.ZERO);
            if (ObjectUtils.isNotEmpty(netRevenue.get(0)) && ObjectUtils.isNotEmpty(netRevenue.get(0).getAmount())) {
                revenueStats.setNetRevenue(netRevenue.get(0).getAmount());
            } else revenueStats.setNetRevenue(BigDecimal.ZERO);
            transactionOverviewResponse.setRevenueStats(revenueStats);

            if (ObjectUtils.isNotEmpty(latestSettlement)) {
                settlementStats.setLatestSettlement(latestSettlement.get(0).getAmount());
                settlementStats.setLatestSettlementDate(latestSettlement.get(0).getSettlementDate());
            } else settlementStats.setLatestSettlement(BigDecimal.ZERO);
            if (ObjectUtils.isNotEmpty(nextSettlement)) {
                settlementStats.setNextSettlement(nextSettlement.get(0).getAmount());
                settlementStats.setNextSettlementDate(nextSettlement.get(0).getSettlementDate());
            } else settlementStats.setNextSettlement(BigDecimal.ZERO);


            transactionOverviewResponse.setYearMonthStats(yearMonthStats);
            transactionOverviewResponse.setSettlementStats(settlementStats);
            transactionOverviewResponse.setStatusStats(statusStats);
            transactionOverviewResponse.setSuccessErrorStats(successErrorStats);
            transactionOverviewResponse.setRefusalErrorStats(refusalErrorStats);
            transactionOverviewResponse.setPaymentChannelStats(paymentChannelStats);

        }
        return transactionOverviewResponse;
    }

    // s-l done
    @Override
    @SuppressWarnings(value = "unchecked")
    public List<TransactionYearMonthStats> getTransactionStatsByYearAndMonth(final String merchantId, final Long year, final Date startDate, final Date endDate, String mode) {
        String tbl;
        if(mode.equals(MerchantTransactionMode.TEST.toString())){
            tbl = "m_sandbox_payment_gateway";
        } else {
            tbl = "m_payment_gateway";
        }
        @NotNull final String FINAL_Q = buildYearMonthQuery(merchantId, year, startDate, endDate, mode);
        var cscFactory = new CallableStatementCreatorFactory(FINAL_Q);
        var csc = cscFactory.newCallableStatementCreator(new HashMap<>());
        var returnedParams = List.<SqlParameter>of(
                new SqlReturnResultSet("year_month_stats", BeanPropertyRowMapper.newInstance(TransactionYearMonthStats.class)));
        Map<String, Object> results = jdbcTemplate.call(csc, returnedParams);
        if (ObjectUtils.isNotEmpty(results))
            return (List<TransactionYearMonthStats>) results.get("year_month_stats");
        return List.of();
    }

    // s-l done
    @Override
    @SuppressWarnings(value = "unchecked")
    public TransactionRevenueStats getTransactionGrossAndNetRevenue(final String merchantId, String mode) {
        @NotNull final String GROSS_REVENUE_Q = getGrossRevenueQuery(merchantId, mode);
        @NotNull final String NET_REVENUE_Q = getNetRevenueQuery(merchantId, mode);
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
            if (ObjectUtils.isNotEmpty(grossRevenue.get(0)) && ObjectUtils.isNotEmpty(grossRevenue.get(0).getAmount())){
                revenueStats.setGrossRevenue(grossRevenue.get(0).getAmount());
            } else {
                revenueStats.setGrossRevenue(BigDecimal.ZERO);
            }
            if (ObjectUtils.isNotEmpty(netRevenue.get(0)) && ObjectUtils.isNotEmpty(netRevenue.get(0).getAmount())) {
                revenueStats.setNetRevenue(netRevenue.get(0).getAmount());
            } else {
                revenueStats.setNetRevenue(BigDecimal.ZERO);
            }

            return revenueStats;
        } else {
            revenueStats.setGrossRevenue(BigDecimal.ZERO);
            revenueStats.setNetRevenue(BigDecimal.ZERO);

            return revenueStats;
        }
    }

    // s-l done
    private String buildYearMonthQuery(String merchantId, Long year, Date startDate, Date endDate, String mode) {
        String tbl;
        if(mode.equals(MerchantTransactionMode.TEST.toString())){
            tbl = "m_sandbox_payment_gateway";
        } else {
            tbl = "m_payment_gateway";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyy-MM-dd");
        final Long currentYear = ObjectUtils.isEmpty(year) ? Calendar.getInstance().get(Calendar.YEAR) : year;
        @NotNull final String ALT_Q = " merchant_id IS NOT NULL ";
        @NotNull final String MER = String.format(" merchant_id = '%s' ", merchantId);
        @NotNull final String EXTRACT_Q = " EXTRACT( year FROM tran_date ) ";
        @NotNull String DATE_SUB_QUERY;
        if (ObjectUtils.isNotEmpty(startDate) && ObjectUtils.isNotEmpty(endDate))
            DATE_SUB_QUERY = String.format(" CAST(tran_date AS TIMESTAMP) BETWEEN CAST('%s' AS TIMESTAMPTZ) AND CAST('%s' AS TIMESTAMPTZ) ",
                    simpleDateFormat.format(startDate), simpleDateFormat.format(endDate));
        else DATE_SUB_QUERY = String.format(" EXTRACT( year FROM tran_date ) = %s ", currentYear);
        @NotNull final String SUB_Q_MER = ObjectUtils.isNotEmpty(merchantId) ? String.format(" %s ", MER) : String.format(" %s ", ALT_Q);
        @NotNull final String FINAL_Q = String.format(" SELECT %s as year , SUM(amount) AS total_revenue, TO_CHAR(tran_date, 'Mon') AS month FROM %s t WHERE %s AND %s AND status='SUCCESSFUL' GROUP BY month,year ;", EXTRACT_Q, tbl, DATE_SUB_QUERY, SUB_Q_MER);
        return FINAL_Q;
    }

    //TODO: Get the transaction graph by start date and end date
    @Deprecated
    private String buildYearMonthQueryWithDateRange(Date startDate, Date endDate, String merchantId) {
        @NotNull final String ALT_Q = " merchant_id IS NOT NULL ";
        @NotNull final String MER = String.format(" merchant_id = '%s' ", merchantId);
        @NotNull final String EXTRACT_Q = " EXTRACT( year FROM tran_date ) ";
        @NotNull final String SUB_EXTRACT_Q = String.format(" CAST(tran_date AS TIMESTAMP) BETWEEN CAST(%s AS TIMESTAMP) AND CAST(%s AS TIMESTAMP) ", startDate, endDate);
        @NotNull final String SUB_Q_MER = ObjectUtils.isNotEmpty(merchantId) ? String.format(" %s ", MER) : String.format(" %s ", ALT_Q);
        @NotNull final String FINAL_Q = String.format(" SELECT %s as year , SUM(*.amount) AS total_revenue, TO_CHAR(tran_date, 'Mon') AS month, merchant_id FROM m_payment_gateway t " +
                " WHERE %s AND %s AND status='SUCCESSFUL' GROUP BY month,merchant_id,year ;", EXTRACT_Q, SUB_EXTRACT_Q, SUB_Q_MER);
        return FINAL_Q;
    }

    // s-l done
    private String getGrossRevenueQuery(String merchantId, String mode) {
        String tbl;
        if(mode.equals(MerchantTransactionMode.TEST.toString())){
            tbl = "m_sandbox_payment_gateway";
        } else {
            tbl = "m_payment_gateway";
        }
        @NotNull final String SUB_Q_MER_AND = ObjectUtils.isEmpty(merchantId) ? " " : String.format(" AND merchant_id = '%s' ", merchantId);
        return String.format("SELECT SUM(amount) amount FROM %s WHERE status='SUCCESSFUL' %s ;", tbl, SUB_Q_MER_AND);
    }

    // s-l done
    private String getNetRevenueQuery(String merchantId, String mode) {
        String tbl;
        if(mode.equals(MerchantTransactionMode.TEST.toString())){
            tbl = "m_sandbox_payment_gateway";
        } else {
            tbl = "m_payment_gateway";
        }
        @NotNull final String SUB_Q_MER_AND = ObjectUtils.isEmpty(merchantId) ? " " : String.format(" AND merchant_id = '%s' ", merchantId);
        return String.format("SELECT ( SUM(amount) - SUM(fee) ) amount FROM %s WHERE status='SUCCESSFUL' %s ;", tbl, SUB_Q_MER_AND);
    }

    //TODO: Remove after settlement removal
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

    @Override
    @SuppressWarnings(value = "unchecked")
    public List<PaymentGateway> getAllTransactionsByRefNo(final String delimiterRefNo) {
        @NotNull final String P_QUERY = String.format("SELECT * FROM m_payment_gateway WHERE ref_no IN (%s)", delimiterRefNo);
        var cscFactory = new CallableStatementCreatorFactory(P_QUERY);
        var csc = cscFactory.newCallableStatementCreator(new HashMap<>());
        var returnedParams = List.<SqlParameter>of(
                new SqlReturnResultSet("transactions", BeanPropertyRowMapper.newInstance(PaymentGateway.class)));
        Map<String, Object> results = jdbcTemplate.call(csc, returnedParams);
        if (ObjectUtils.isNotEmpty(results))
            return (List<PaymentGateway>) results.get("transactions");
        return List.of();
    }

    @SuppressWarnings(value = "unchecked")
    @Override
    public Page<TransactionSettlementPojo> getAllTransactionSettlement(SettlementQueryPojo settlementQueryPojo, String merchantId, Pageable pageable) {
        @NotNull final String STATUS_QUERY = ObjectUtils.isNotEmpty(settlementQueryPojo.getStatus()) ? String.format(" AND mts.settlement_status='%s' ", settlementQueryPojo.getStatus()) : " ";
        @NotNull final String SUB_Q_MER_AND = ObjectUtils.isEmpty(merchantId) ? " " : String.format(" AND mpg.merchant_id = '%s' ", merchantId);
        int limit = pageable.getPageSize();
        Long offSet = pageable.getOffset();
        var cscFactory = new CallableStatementCreatorFactory(String.format("SELECT mpg.ref_no settlement_reference_id, mts.settlement_status, " +
                " mpg.amount settlement_gross_amount,mpg.amount - mpg.fee settlement_net_amount, mpg.wayapay_fee wayapay_fee, mpg.processing_fee processing_fee, " +
                " mpg.fee fee, mpg.merchant_id , mts.settlement_account, mts.account_settlement_option, " +
                " mts.settlement_beneficiary_account, mts.merchant_configured_settlement_date settlement_date, mpg.merchant_id, mpg.merchant_name " +
                " FROM m_payment_gateway mpg INNER JOIN m_transaction_settlement mts ON mts.settlement_reference_id = mpg.settlement_reference_id " +
                " WHERE status='SUCCESSFUL' %s %s LIMIT %s OFFSET %s;", SUB_Q_MER_AND, STATUS_QUERY, limit, offSet) +
                String.format(" SELECT COUNT(*) FROM m_payment_gateway mpg " +
                        " INNER JOIN m_transaction_settlement mts ON mts.settlement_reference_id = mpg.settlement_reference_id" +
                        " WHERE status='SUCCESSFUL' %s %s;", SUB_Q_MER_AND, STATUS_QUERY));
        var returnedParams = Arrays.<SqlParameter>asList(
                new SqlReturnResultSet("transaction_settlement", BeanPropertyRowMapper.newInstance(TransactionSettlementPojo.class)),
                new SqlReturnResultSet("count", BeanPropertyRowMapper.newInstance(CountWrapper.class)));
        CallableStatementCreator csc = cscFactory.newCallableStatementCreator(new HashMap<>());
        Map<String, Object> results = jdbcTemplate.call(csc, returnedParams);
        return PageableExecutionUtils.getPage((List<TransactionSettlementPojo>) results.get("transaction_settlement"),
                pageable, () -> ((ArrayList<CountWrapper>) results.get("count")).get(0).getCount().longValue());
    }

    // s-l done
    @Override
    public void expireAllTransactionMoreThan30Mins() {
        String UPDATE_QUERY = "UPDATE m_payment_gateway SET transaction_expired=true WHERE transaction_expired=false AND EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - tran_date ))/60 >= 50.0000";
        int updatedRows = jdbcTemplate.update(UPDATE_QUERY);
        if (!(updatedRows >= 0))
            throw new ApplicationException(400, "01", "Failed to update live transaction expiry");

        String UPDATE_SANDBOX_QUERY = " UPDATE m_sandbox_payment_gateway SET transaction_expired=true WHERE transaction_expired = false AND EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - tran_date ))/60 >= 50.0000 ";
        int updatedSandboxRows = jdbcTemplate.update(UPDATE_SANDBOX_QUERY);
        if (!(updatedSandboxRows >= 0))
            throw new ApplicationException(400, "01", "Failed to update sandbox transaction expiry");
    }


    //can be used to get all the grouped settlement
    private String getMerchantSettlementStatsQuery(String merchantId) {
        @NotNull final String SUB_Q_MER_AND = ObjectUtils.isEmpty(merchantId) ? " " : String.format(" AND merchant_id = '%s' ", merchantId);
        return String.format("SELECT merchant_id, SUM(fee) as total_fee, SUM(amount) gross_amount, " +
                " (SUM(amount) - SUM(fee) ) net_amount FROM m_payment_gateway WHERE status = 'SUCCESSFUL' %s" +
                " AND settlement_status = 'PENDING' GROUP BY merchant_id ", SUB_Q_MER_AND);
    }

    //Get all the transactions to be settled
    private String getAllTransactionToBeSettled(String merchantId) {
        @NotNull final String SUB_Q_MER_AND = ObjectUtils.isEmpty(merchantId) ? " " : String.format(" AND merchant_id = '%s' ", merchantId);
        return String.format("SELECT * FROM m_payment_gateway WHERE status = 'SUCCESSFUL' %s AND settlement_status = 'PENDING' GROUP BY merchant_id ", SUB_Q_MER_AND);
    }
}
