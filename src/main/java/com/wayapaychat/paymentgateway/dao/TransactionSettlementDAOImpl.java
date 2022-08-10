package com.wayapaychat.paymentgateway.dao;


import com.wayapaychat.paymentgateway.common.enums.MerchantTransactionMode;
import com.wayapaychat.paymentgateway.enumm.SettlementStatus;
import com.wayapaychat.paymentgateway.mapper.BigDecimalAmountWrapper;
import com.wayapaychat.paymentgateway.mapper.SettlementWrapper;
import com.wayapaychat.paymentgateway.pojo.waya.stats.BigDecimalCountStatusWrapper;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionSettlementStats;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionSettlementsResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TransactionSettlementDAOImpl implements TransactionSettlementDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;


    @SuppressWarnings("unchecked")
    @Override
    public TransactionSettlementsResponse merchantTransactionSettlementStats(String merchantId, String mode) {
        String pg_tbl, ts_tbl;
        if (mode == MerchantTransactionMode.PRODUCTION.toString()){
            pg_tbl = "m_payment_gateway";
            ts_tbl = "m_transaction_settlement";
        } else {
            pg_tbl = "m_sandbox_payment_gateway";
            ts_tbl = "m_sandbox_transaction_settlement";
        }
        @NotNull String MER_Q = ObjectUtils.isEmpty(merchantId) ? " merchant_id IS NOT NULL " : String.format(" merchant_id = '%s' ", merchantId);
        @NotNull final String MERCHANT_SETTLEMENT_STATS_Q = String.format("SELECT COUNT(settlement_status), settlement_status as status FROM %s WHERE %s GROUP BY settlement_status;", pg_tbl, MER_Q);
        @NotNull final String LATEST_SETTLEMENT = getLatestSettlementQuery(merchantId);
        @NotNull final String NEXT_SETTLEMENT = getNextSettlementQuery(merchantId);
        @NotNull final String NET_SETTLED_TRANSACTIONS = getNetSettledTransactionQuery(merchantId, mode);
        @NotNull final String FAILED_NET_SETTLEMENT_TRANSACTIONS = String.format("SELECT SUM(settlement_net_amount) as amount FROM %s WHERE %s AND settlement_status='FAILED';", ts_tbl, MER_Q);
        @NotNull final String FINAL_Q = MERCHANT_SETTLEMENT_STATS_Q + LATEST_SETTLEMENT + NEXT_SETTLEMENT + NET_SETTLED_TRANSACTIONS + FAILED_NET_SETTLEMENT_TRANSACTIONS;
        var csf = new CallableStatementCreatorFactory(FINAL_Q);
        var csc = csf.newCallableStatementCreator(new HashMap<>());
        var requestParams = List.<SqlParameter>of(
                new SqlReturnResultSet("stats_count", BeanPropertyRowMapper.newInstance(BigDecimalCountStatusWrapper.class)),
                new SqlReturnResultSet("latest_settlement", BeanPropertyRowMapper.newInstance(BigDecimalAmountWrapper.class)),
                new SqlReturnResultSet("next_settlement", BeanPropertyRowMapper.newInstance(BigDecimalAmountWrapper.class)),
                new SqlReturnResultSet("net_settled_transactions", BeanPropertyRowMapper.newInstance(BigDecimalAmountWrapper.class)),
                new SqlReturnResultSet("failed_net_settlement_transactions", BeanPropertyRowMapper.newInstance(BigDecimalAmountWrapper.class)));
        Map<String, Object> results = jdbcTemplate.call(csc, requestParams);
        TransactionSettlementsResponse transactionSettlementsResponse = TransactionSettlementsResponse.builder().build();
        if (ObjectUtils.isNotEmpty(results)) {
            List<BigDecimalCountStatusWrapper> statsCount = (List<BigDecimalCountStatusWrapper>) results.get("stats_count");
            List<BigDecimalAmountWrapper> latestSettlement = (List<BigDecimalAmountWrapper>) results.get("latest_settlement");
            List<BigDecimalAmountWrapper> nextSettlement = (List<BigDecimalAmountWrapper>) results.get("next_settlement");
            List<BigDecimalAmountWrapper> netSettledTransactions = (List<BigDecimalAmountWrapper>) results.get("net_settled_transactions");
            List<BigDecimalAmountWrapper> failedNetSettledTransactions = (List<BigDecimalAmountWrapper>) results.get("failed_net_settlement_transactions");
            TransactionSettlementStats transactionSettlementStats = TransactionSettlementStats.builder().build();
            if (ObjectUtils.isNotEmpty(latestSettlement) && ObjectUtils.isNotEmpty(latestSettlement.get(0).getAmount()))
                transactionSettlementStats.setLatestSettlement(latestSettlement.get(0).getAmount());
            else transactionSettlementStats.setLatestSettlement(BigDecimal.ZERO);
            if (ObjectUtils.isNotEmpty(nextSettlement) && ObjectUtils.isNotEmpty(nextSettlement.get(0).getAmount()))
                transactionSettlementStats.setNextSettlement(nextSettlement.get(0).getAmount());
            else transactionSettlementStats.setNextSettlement(BigDecimal.ZERO);
            if (ObjectUtils.isNotEmpty(netSettledTransactions) && ObjectUtils.isNotEmpty(netSettledTransactions.get(0).getAmount()))
                transactionSettlementStats.setNetRevenue(netSettledTransactions.get(0).getAmount());
            else transactionSettlementStats.setNetRevenue(BigDecimal.ZERO);
            if (ObjectUtils.isNotEmpty(failedNetSettledTransactions) && ObjectUtils.isNotEmpty(failedNetSettledTransactions.get(0).getAmount()))
                transactionSettlementStats.setFailedNetSettledRevenue(failedNetSettledTransactions.get(0).getAmount());
            else transactionSettlementStats.setFailedNetSettledRevenue(BigDecimal.ZERO);
            transactionSettlementsResponse.setStats(transactionSettlementStats);

            List<SettlementStatus> settlementStatuses = List.of(SettlementStatus.SETTLED, SettlementStatus.FAILED, SettlementStatus.PENDING);
            List<BigDecimalCountStatusWrapper> statsCountStatusMissing = new ArrayList<>();
            settlementStatuses.stream().sequential().forEach(settlementStatus -> {
                boolean found = statsCount.stream()
                        .map(bigDecimalCountStatusWrapper -> SettlementStatus.valueOf(bigDecimalCountStatusWrapper.getStatus()))
                        .anyMatch(status -> settlementStatus == status);
                if (!found) {
                    statsCountStatusMissing.add(BigDecimalCountStatusWrapper
                            .builder()
                            .count(new BigDecimal("0"))
                            .status(settlementStatus.name())
                            .build());
                }
            });
            statsCount.addAll(statsCountStatusMissing);
            transactionSettlementsResponse.setCounts(statsCount);
            return transactionSettlementsResponse;
        }
        return transactionSettlementsResponse;
    }

    private String getNetSettledTransactionQuery(String merchantId, String mode) {
        String ts_tbl;
        if (mode == MerchantTransactionMode.PRODUCTION.toString()){
            ts_tbl = "m_transaction_settlement";
        } else {
            ts_tbl = "m_sandbox_transaction_settlement";
        }
        @NotNull String MER_Q = ObjectUtils.isEmpty(merchantId) ? " merchant_id IS NOT NULL " : " merchant_id = '%s' ";
        @NotNull final String LATEST_SETTLEMENT_Q = String.format("SELECT SUM(settlement_net_amount) as amount FROM %s WHERE %s AND settlement_status='SETTLED'; ", ts_tbl, MER_Q, merchantId);
        return LATEST_SETTLEMENT_Q;
    }

    @Override
    public String getLatestSettlementQuery(String merchantId) {
        @NotNull String SUB = ObjectUtils.isEmpty(merchantId) ? " " : String.format(" AND merchant_id='%s' ", merchantId);
        @NotNull final String LATEST_SETTLEMENT_Q = String.format("SELECT settlement_net_amount as amount,date_settled as settlement_date  FROM m_transaction_settlement WHERE settlement_status='SETTLED' " +
                " %s ORDER BY date_settled DESC LIMIT 1 ;", SUB);
        return LATEST_SETTLEMENT_Q;
    }

    @Override
    public String getNextSettlementQuery(String merchantId) {
        @NotNull String SUB = ObjectUtils.isEmpty(merchantId) ? " " : String.format(" AND merchant_id='%s' ", merchantId);
        @NotNull final String NEXT_SETTLEMENT_Q = String.format("SELECT settlement_net_amount as amount,merchant_configured_settlement_date settlement_date " +
                " FROM m_transaction_settlement WHERE settlement_status='PENDING' " +
                " %s ORDER BY merchant_configured_settlement_date DESC LIMIT 1 ;", SUB);
        return NEXT_SETTLEMENT_Q;
    }
}
