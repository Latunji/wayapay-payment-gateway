package com.wayapaychat.paymentgateway.service.impl;

import com.wayapaychat.paymentgateway.common.utils.PaymentGateWayCommonUtils;
import com.wayapaychat.paymentgateway.entity.RecurrentTransaction;
import com.wayapaychat.paymentgateway.exception.ApplicationException;
import com.wayapaychat.paymentgateway.pojo.waya.*;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantData;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantResponse;
import com.wayapaychat.paymentgateway.proxy.IdentityManager;
import com.wayapaychat.paymentgateway.repository.RecurrentTransactionRepository;
import com.wayapaychat.paymentgateway.service.RecurrentTransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@Slf4j
@AllArgsConstructor
public class RecurrentTransactionServiceImpl implements RecurrentTransactionService {
    private final RecurrentTransactionRepository recurrentTransactionRepository;
    private final IdentityManager identityManager;
    private final PaymentGateWayCommonUtils paymentGateWayCommonUtils;

    @Override
    public ResponseEntity<PaymentGatewayResponse> filterSearchRecurrentTransaction(QueryRecurrentTransactionPojo queryCustomerTransactionPojo, Pageable pageable) {
        return null;
    }

    @Override
    public ResponseEntity<PaymentGatewayResponse> fetchCustomerTransaction(String customerId, Pageable pageable) {
        AuthenticatedUser authenticatedUser = PaymentGateWayCommonUtils.getAuthenticatedUser();
        String token = paymentGateWayCommonUtils.getDaemonAuthToken();
        MerchantResponse merchantResponse = identityManager.getMerchantDetail(token, authenticatedUser.getMerchantId());
        MerchantData merchantData = merchantResponse.getData();
        String merchantId = merchantData.getMerchantId();
        return new ResponseEntity<>(new SuccessResponse("Data fetched successfully",
                getCustomerRecurrentTransactions(customerId, merchantId, pageable)), HttpStatus.OK);
    }

    @Override
    public Page<RecurrentTransaction> getCustomerRecurrentTransactions(final String customerId, final String merchantId, Pageable pageable) {
        return recurrentTransactionRepository.getTransactionByCustomerId(customerId, merchantId, pageable);
    }

    @Override
    public ResponseEntity<PaymentGatewayResponse> getCustomerRecurrentTransaction(String merchantId, String customerId, Pageable pageable) {
        return new ResponseEntity<>(new SuccessResponse("Data fetched successfully",
                getCustomerRecurrentTransactionById(customerId, merchantId)), HttpStatus.OK);
    }

    @Override
    public RecurrentTransaction getCustomerRecurrentTransactionById(final String recurrentTransactionId, final String merchantId) {
        Optional<RecurrentTransaction> optionalRecurrentTransaction = recurrentTransactionRepository.getByRecurrentTransactionId(recurrentTransactionId, merchantId);
        if (optionalRecurrentTransaction.isPresent())
            return optionalRecurrentTransaction.get();
        else
            throw new ApplicationException(404, "01", "Not found");
    }
}
