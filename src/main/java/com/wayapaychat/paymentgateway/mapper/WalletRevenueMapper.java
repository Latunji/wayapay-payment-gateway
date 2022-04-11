package com.wayapaychat.paymentgateway.mapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.wayapaychat.paymentgateway.pojo.waya.wallet.WalletRevenue;

public class WalletRevenueMapper implements RowMapper<WalletRevenue> {

	@Override
	public WalletRevenue mapRow(ResultSet rs, int rowNum) throws SQLException {
		BigDecimal totAmount = rs.getBigDecimal("TOTALUNSETTLEDAMT");
		BigDecimal netAmount = rs.getBigDecimal("TOTALSETTLEDAMT");
		int totalTransaction = rs.getInt("TOTALTRAN");
		int totalSuccess = rs.getInt("TOTALSUCCESS");
		int totalFailed = rs.getInt("TOTALFAILED");
		int totalAbandoned = rs.getInt("TOTALABANDONED");
		int totalPending = rs.getInt("TOTALPENDING");
		int totalSettled = rs.getInt("TOTALSETTLED");
		int totalRefunded = rs.getInt("TOTALREFUNDED");
		String merchantId = rs.getString("MERCHANTID");
		BigDecimal grossAmount = totAmount.add(netAmount);
		WalletRevenue revenue = new WalletRevenue();
		revenue.setGrossAmount(grossAmount);
		revenue.setNetAmount(netAmount);
		revenue.setTotalTransaction(totalTransaction);
		revenue.setTotalSuccess(totalSuccess);
		revenue.setTotalFailed(totalFailed);
		revenue.setTotalAbandoned(totalAbandoned);
		revenue.setTotalPending(totalPending);
		revenue.setTotalSettled(totalSettled);
		revenue.setTotalRefunded(totalRefunded);
		revenue.setMerchantId(merchantId);
		return revenue;
	}

}
