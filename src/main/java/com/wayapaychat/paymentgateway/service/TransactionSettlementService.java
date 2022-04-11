package com.wayapaychat.paymentgateway.service;


import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface TransactionSettlementService {
    ResponseEntity<PaymentGatewayResponse> getMerchantSettlementStats(String merchantId);

    ResponseEntity<PaymentGatewayResponse> getMerchantSettlements(String merchantId, Pageable dateSettled);
}
