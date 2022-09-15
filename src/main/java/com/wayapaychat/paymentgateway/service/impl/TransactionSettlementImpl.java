package com.wayapaychat.paymentgateway.service.impl;

import com.wayapaychat.paymentgateway.common.enums.MerchantTransactionMode;
import com.wayapaychat.paymentgateway.common.utils.PaymentGateWayCommonUtils;
import com.wayapaychat.paymentgateway.dao.TransactionSettlementDAO;
import com.wayapaychat.paymentgateway.dao.WayaPaymentDAO;
import com.wayapaychat.paymentgateway.entity.TransactionSettlement;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.waya.SettlementQueryPojo;
import com.wayapaychat.paymentgateway.pojo.waya.SuccessResponse;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantData;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantResponse;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionSettlementsResponse;
import com.wayapaychat.paymentgateway.proxy.IdentityManagementServiceProxy;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import com.wayapaychat.paymentgateway.repository.TransactionSettlementRepository;
import com.wayapaychat.paymentgateway.service.TransactionSettlementService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class TransactionSettlementImpl implements TransactionSettlementService {
    private TransactionSettlementRepository transactionSettlementRepository;
    private PaymentGatewayRepository paymentGatewayRepository;
    private final IdentityManagementServiceProxy identityManagementServiceProxy;
    private final PaymentGateWayCommonUtils paymentGateWayCommonUtils;
    private TransactionSettlementDAO transactionSettlementDAO;
    private WayaPaymentDAO wayaPaymentDAO;

    @Override
    public ResponseEntity<PaymentGatewayResponse> getSettlementStats(String merchantId) {
        String merchantIdToUse = PaymentGateWayCommonUtils.getMerchantIdToUse(merchantId,false);
        String mode = MerchantTransactionMode.PRODUCTION.name();
        if (ObjectUtils.isNotEmpty(merchantIdToUse)) {
            String token = paymentGateWayCommonUtils.getDaemonAuthToken();
            MerchantResponse merchantResponse = identityManagementServiceProxy.getMerchantDetail(token, merchantIdToUse);
            MerchantData merchantData = merchantResponse.getData();
            mode = merchantData.getMerchantKeyMode();
        }
        
        TransactionSettlementsResponse data = transactionSettlementDAO.merchantTransactionSettlementStats(merchantIdToUse, mode);
        return new ResponseEntity<>(new SuccessResponse("Data successfully fetched", data), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PaymentGatewayResponse> getCumulativeTransactionSettlement(SettlementQueryPojo settlementQueryPojo, String merchantId, Pageable pageable) {
        Page<TransactionSettlement> data;
        String merchantIdToUse = PaymentGateWayCommonUtils.getMerchantIdToUse(merchantId,true);
        if (ObjectUtils.isNotEmpty(settlementQueryPojo.getStatus()) && ObjectUtils.isEmpty(settlementQueryPojo.getStartSettlementDate()))
            data = transactionSettlementRepository.findAllWithStatus(merchantIdToUse, settlementQueryPojo.getStatus().name(), pageable);
        else if (ObjectUtils.isNotEmpty(settlementQueryPojo.getStatus()) && ObjectUtils.isNotEmpty(settlementQueryPojo.getStartSettlementDate())
                && ObjectUtils.isEmpty(settlementQueryPojo.getEndSettlementDate()))
            data = transactionSettlementRepository.findAllWithSettlementDateStatus(
                    merchantIdToUse,
                    settlementQueryPojo.getStatus().name(),
                    settlementQueryPojo.getStartSettlementDate(),
                    pageable
            );
        else if (ObjectUtils.isNotEmpty(settlementQueryPojo.getStatus()) && ObjectUtils.isNotEmpty(settlementQueryPojo.getStartSettlementDate())
                && ObjectUtils.isNotEmpty(settlementQueryPojo.getEndSettlementDate()))
            data = transactionSettlementRepository.findAllWithStartEndDatesStatus(
                    merchantIdToUse,
                    settlementQueryPojo.getStatus().name(),
                    settlementQueryPojo.getStartSettlementDate(),
                    settlementQueryPojo.getEndSettlementDate(),
                    pageable
            );
        else if (ObjectUtils.isEmpty(settlementQueryPojo.getStatus()) && ObjectUtils.isNotEmpty(settlementQueryPojo.getStartSettlementDate())
                && ObjectUtils.isNotEmpty(settlementQueryPojo.getEndSettlementDate()))
            data = transactionSettlementRepository.findAllWithStartEndDates(
                    merchantIdToUse,
                    settlementQueryPojo.getStartSettlementDate(),
                    settlementQueryPojo.getEndSettlementDate(),
                    pageable
            );
        else
            data = transactionSettlementRepository.findAll(merchantIdToUse, pageable);
        return new ResponseEntity<>(new SuccessResponse("Data successfully fetched", data), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PaymentGatewayResponse> getAllSettledSuccessfulTransactions(SettlementQueryPojo settlementQueryPojo, String merchantId, Pageable pageable) {
        Page<TransactionSettlementPojo> data;
        String merchantIdToUse = PaymentGateWayCommonUtils.getMerchantIdToUse(merchantId,false);
        data = wayaPaymentDAO.getAllTransactionSettlement(settlementQueryPojo,merchantIdToUse, pageable);
        return new ResponseEntity<>(new SuccessResponse("Data successfully fetched", data), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PaymentGatewayResponse> getSettlementByReferenceId(String settlementReferenceId) {
        return new ResponseEntity<>(new SuccessResponse("Search Completed", transactionSettlementRepository.getTransactionSettlementBySettlementReferenceId(settlementReferenceId)), HttpStatus.OK);
    }
}
