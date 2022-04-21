package com.wayapaychat.paymentgateway.service;


import com.wayapaychat.paymentgateway.entity.RecurrentTransaction;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.waya.QueryRecurrentTransactionPojo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface RecurrentTransactionService {
    ResponseEntity<PaymentGatewayResponse> filterSearchRecurrentTransaction(QueryRecurrentTransactionPojo queryRecurrentTransactionPojo, Pageable pageable);

    ResponseEntity<PaymentGatewayResponse> fetchCustomerTransaction(String customerId, Pageable pageable);

    Page<RecurrentTransaction> getCustomerRecurrentTransactions(String merchantId, String customerId, Pageable pageable);

    ResponseEntity<PaymentGatewayResponse> getCustomerRecurrentTransaction(String merchantId, String customerId, Pageable pageable);

    RecurrentTransaction getCustomerRecurrentTransactionById(String recurrentTransactionId, String merchantId);
}
