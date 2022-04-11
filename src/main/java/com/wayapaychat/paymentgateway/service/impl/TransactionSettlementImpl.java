package com.wayapaychat.paymentgateway.service.impl;

import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.service.TransactionSettlementService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class TransactionSettlementImpl implements TransactionSettlementService {

    @Override
    public ResponseEntity<PaymentGatewayResponse> getMerchantSettlementStats(String merchantId) {
        return null;
    }

    @Override
    public ResponseEntity<PaymentGatewayResponse> getMerchantSettlements(String merchantId, Pageable dateSettled) {
        return null;
    }
}
