package com.wayapaychat.paymentgateway.dao;

import java.util.List;

import com.wayapaychat.paymentgateway.pojo.waya.WalletRevenue;

public interface WayaPaymentDAO {
	
	public List<WalletRevenue> getRevenue();
	
	public WalletRevenue getRevenue(String merchantId);

}
