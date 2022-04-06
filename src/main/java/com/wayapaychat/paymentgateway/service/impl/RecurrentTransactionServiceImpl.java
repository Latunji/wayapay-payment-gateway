package com.wayapaychat.paymentgateway.service.impl;

import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.waya.QueryRecurrentTransactionPojo;
import com.wayapaychat.paymentgateway.repository.RecurrentTransactionRepository;
import com.wayapaychat.paymentgateway.service.RecurrentTransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@AllArgsConstructor
public class RecurrentTransactionServiceImpl implements RecurrentTransactionService {
    private final RecurrentTransactionRepository recurrentTransactionRepository;
    @Override
    public ResponseEntity<PaymentGatewayResponse> filterSearchRecurrentTransaction(QueryRecurrentTransactionPojo queryCustomerTransactionPojo, Pageable pageable) {
        return null;
    }

    @Override
    public ResponseEntity<PaymentGatewayResponse> fetchCustomerTransaction(String customerId, Pageable pageable) {
        return null;
    }

}
