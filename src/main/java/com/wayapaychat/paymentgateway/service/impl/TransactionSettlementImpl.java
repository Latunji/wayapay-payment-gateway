package com.wayapaychat.paymentgateway.service.impl;

import com.wayapaychat.paymentgateway.common.enums.Constant;
import com.wayapaychat.paymentgateway.common.enums.MerchantTransactionMode;
import com.wayapaychat.paymentgateway.common.utils.PaymentGateWayCommonUtils;
import com.wayapaychat.paymentgateway.dao.TransactionSettlementDAO;
import com.wayapaychat.paymentgateway.dao.WayaPaymentDAO;
import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.entity.TransactionSettlement;
import com.wayapaychat.paymentgateway.enumm.MerchantPermissions;
import com.wayapaychat.paymentgateway.pojo.RolePermissionResponsePayload;
import com.wayapaychat.paymentgateway.pojo.RoleResponse;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentListResponse;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Autowired
    private RoleProxy roleProxy;

    @Override
    public ResponseEntity<PaymentGatewayResponse> getSettlementStats(String merchantId) {
        String token = null;
        MerchantResponse merchantResponse = null;
        String merchantIdToUse = PaymentGateWayCommonUtils.getMerchantIdToUse(merchantId,false);
        String mode = MerchantTransactionMode.PRODUCTION.name();
        if (ObjectUtils.isNotEmpty(merchantIdToUse)) {
            token = paymentGateWayCommonUtils.getDaemonAuthToken();
            merchantResponse = identityManagementServiceProxy.getMerchantDetail(token, merchantIdToUse);
            MerchantData merchantData = merchantResponse.getData();
            mode = merchantData.getMerchantKeyMode();
        }
//        RolePermissionResponsePayload response = roleProxy.fetchUserRoleAndPermissions(merchantResponse.getData().getUserId(), token);
//        if (response.getPermissions().contains(MerchantPermissions.CAN_VIEW_DASHBOARD_OVERVIEW)) {
            TransactionSettlementsResponse data = transactionSettlementDAO.merchantTransactionSettlementStats(merchantIdToUse, mode);
            return new ResponseEntity<>(new SuccessResponse("Data successfully fetched", data), HttpStatus.OK);
//        }else{
//            return new ResponseEntity<>(new SuccessResponse(Constant.PERMISSION_ERROR), HttpStatus.NOT_FOUND);
//        }
    }

    @Override
    public ResponseEntity<PaymentGatewayResponse> getCumulativeTransactionSettlement(SettlementQueryPojo settlementQueryPojo, String merchantId, Pageable pageable) {
        Page<TransactionSettlement> data;
        String merchantIdToUse = PaymentGateWayCommonUtils.getMerchantIdToUse(merchantId,true);
        String token = paymentGateWayCommonUtils.getDaemonAuthToken();
        MerchantResponse merchantResponse = identityManagementServiceProxy.getMerchantDetail(token, merchantIdToUse);
//        RolePermissionResponsePayload response = roleProxy.fetchUserRoleAndPermissions(merchantResponse.getData().getUserId(), token);
//        if (response.getPermissions().contains(MerchantPermissions.CAN_VIEW_SETTLEMENTS)) {
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
//        }else{
//            return new ResponseEntity<>(new SuccessResponse(Constant.PERMISSION_ERROR), HttpStatus.NOT_FOUND);
//        }
    }

    @Override
    public ResponseEntity<PaymentGatewayResponse> getAllSettledSuccessfulTransactions(SettlementQueryPojo settlementQueryPojo, String merchantId, Pageable pageable) {
        Page<TransactionSettlementPojo> data;
        String token = paymentGateWayCommonUtils.getDaemonAuthToken();
        MerchantResponse merchantResponse = identityManagementServiceProxy.getMerchantDetail(token, merchantId);
        String merchantIdToUse = PaymentGateWayCommonUtils.getMerchantIdToUse(merchantId,false);
//        RolePermissionResponsePayload response = roleProxy.fetchUserRoleAndPermissions(merchantResponse.getData().getUserId(), token);
//        if (response.getPermissions().contains(MerchantPermissions.CAN_VIEW_SETTLEMENTS)) {
            data = wayaPaymentDAO.getAllTransactionSettlement(settlementQueryPojo, merchantIdToUse, pageable);
            return new ResponseEntity<>(new SuccessResponse("Data successfully fetched", data), HttpStatus.OK);
//        }else{
//            return new ResponseEntity<>(new SuccessResponse(Constant.PERMISSION_ERROR), HttpStatus.NOT_FOUND);
//
//        }
    }

    @Override
    public ResponseEntity<PaymentGatewayResponse> getSettlementByReferenceId(String settlementReferenceId) {
        return new ResponseEntity<>(new SuccessResponse("Search Completed", transactionSettlementRepository.getTransactionSettlementBySettlementReferenceId(settlementReferenceId)), HttpStatus.OK);
    }

    @Override
    public PaymentGatewayResponse fetchAllTransactionsPendingSettlement() {
        List<PaymentGateway> allPayments = paymentGatewayRepository.getAllTransactionNotSettled();
        return new PaymentGatewayResponse(true, "Retrieved Successfully", allPayments);
    }

    @Override
    public PaymentListResponse fetchAllMerchantTransactionsPendingSettlement(String merchantId) {
        List<PaymentGateway> allPayments = paymentGatewayRepository.getAllTransactionNotSettled(merchantId);
        return new PaymentListResponse(true, "Retrieved Successfully", allPayments);
    }
}
