package com.wayapaychat.paymentgateway.pojo.waya.wallet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TransactionReportStats {
	private long totalTransaction;
	private long totalSuccess;
	private long totalFailed;
	private long totalAbandoned;
	private long totalPending;
	private long totalSettled;
	private long totalRefunded;
}
