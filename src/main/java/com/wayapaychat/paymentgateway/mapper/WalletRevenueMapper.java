package com.wayapaychat.paymentgateway.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.wayapaychat.paymentgateway.pojo.waya.wallet.TransactionReportStats;

public class WalletRevenueMapper implements RowMapper<TransactionReportStats> {

	@Override
	public TransactionReportStats mapRow(ResultSet rs, int rowNum) throws SQLException {
		int totalTransaction = rs.getInt("TOTALTRAN");
		int totalSuccess = rs.getInt("TOTALSUCCESS");
		int totalFailed = rs.getInt("TOTALFAILED");
		int totalAbandoned = rs.getInt("TOTALABANDONED");
		int totalPending = rs.getInt("TOTALPENDING");
		int totalSettled = rs.getInt("TOTALSETTLED");
		int totalRefunded = rs.getInt("TOTALREFUNDED");
		TransactionReportStats revenue = new TransactionReportStats();
		revenue.setTotalTransaction(totalTransaction);
		revenue.setTotalSuccess(totalSuccess);
		revenue.setTotalFailed(totalFailed);
		revenue.setTotalAbandoned(totalAbandoned);
		revenue.setTotalPending(totalPending);
		revenue.setTotalSettled(totalSettled);
		revenue.setTotalRefunded(totalRefunded);
		return revenue;
	}

}
