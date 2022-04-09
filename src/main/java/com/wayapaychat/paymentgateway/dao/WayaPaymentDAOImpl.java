package com.wayapaychat.paymentgateway.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.wayapaychat.paymentgateway.mapper.WalletRevenueMapper;
import com.wayapaychat.paymentgateway.pojo.waya.WalletRevenue;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class WayaPaymentDAOImpl implements WayaPaymentDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

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


    ///report/graph-stats?year=2020
    ///report/pie-chart-stats

    //{
    //success : 0,
    //pending : 0,
    //abandoned : 0,
    //failed : 0,
    //refunded : 0,
    //grossRevenue: 0,
    //netRevenue: 0,
    //lastSettlement:0 ,
    //nextSettlement: 0 ,
    //}


    // SELECT COUNT(*.status), status

    //successRate: {
    //	successFul : 0,
    //	error: 0,
    //},
    //

    //refusalReasons: {
    //	systemError: 0,
    //	bankError : 0,
    //	customerError: 0 ,
    //	fraudError: 0,
    //},

    //paymentMethods: {
    //	card: percentage,
    //	ussd: percentage,
    //	bank : percentage,
    //	wallet: percentage,
    //	qrCode : percentage,
    //	payAttitude: percentage
    // }
    //}

}
