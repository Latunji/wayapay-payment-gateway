package com.wayapaychat.paymentgateway.dao;

import com.wayapaychat.paymentgateway.pojo.waya.WalletRevenue;

import java.util.List;

public interface WayaPaymentDAO {

    List<WalletRevenue> getRevenue();

    WalletRevenue getRevenue(String merchantId);

}
