package com.wayapaychat.paymentgateway.service;


import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.waya.SettlementQueryPojo;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface TransactionSettlementService {
    ResponseEntity<PaymentGatewayResponse> getMerchantSettlementStats(String merchantId);

    ResponseEntity<PaymentGatewayResponse> getMerchantSettlements(SettlementQueryPojo settlementQueryPojo, String merchantId, Pageable dateSettled);
}
