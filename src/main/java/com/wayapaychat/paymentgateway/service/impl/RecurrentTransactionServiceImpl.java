package com.wayapaychat.paymentgateway.service.impl;

import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.entity.RecurrentTransaction;
import com.wayapaychat.paymentgateway.pojo.waya.MerchantData;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.waya.QueryRecurrentTransactionPojo;
import com.wayapaychat.paymentgateway.pojo.waya.SuccessResponse;
import com.wayapaychat.paymentgateway.repository.RecurrentTransactionRepository;
import com.wayapaychat.paymentgateway.service.RecurrentTransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@AllArgsConstructor
public class RecurrentTransactionServiceImpl implements RecurrentTransactionService {
    private final RecurrentTransactionRepository recurrentTransactionRepository;
    private final MerchantProxy merchantProxy;

    @Override
    public ResponseEntity<PaymentGatewayResponse> filterSearchRecurrentTransaction(QueryRecurrentTransactionPojo queryCustomerTransactionPojo, Pageable pageable) {
        return null;
    }

    @Override
    public ResponseEntity<PaymentGatewayResponse> fetchCustomerTransaction(String customerId, Pageable pageable) {
        MerchantData merchantResponse = merchantProxy.getMerchantAccount().getData();
        String merchantId = merchantResponse.getMerchantId();
        return new ResponseEntity<>(new SuccessResponse("Data fetched successfully",
                getCustomerTransaction(customerId, merchantId, pageable)), HttpStatus.OK);
    }

    @Override
    public Page<RecurrentTransaction> getCustomerTransaction(final String customerId, final String merchantId, Pageable pageable) {
        return recurrentTransactionRepository.getTransactionByCustomerId(customerId, merchantId, pageable);
    }
}
