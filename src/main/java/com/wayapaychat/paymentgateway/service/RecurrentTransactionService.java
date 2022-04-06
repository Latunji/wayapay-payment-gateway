package com.wayapaychat.paymentgateway.service;


import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.waya.QueryRecurrentTransactionPojo;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface RecurrentTransactionService {
    ResponseEntity<PaymentGatewayResponse> filterSearchRecurrentTransaction(QueryRecurrentTransactionPojo queryRecurrentTransactionPojo, Pageable pageable);

    ResponseEntity<PaymentGatewayResponse> fetchCustomerTransaction(String customerId, Pageable pageable);
}
