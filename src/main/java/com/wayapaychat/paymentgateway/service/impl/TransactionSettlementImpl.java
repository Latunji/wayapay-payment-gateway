package com.wayapaychat.paymentgateway.service.impl;

import com.wayapaychat.paymentgateway.common.enums.Constant;
import com.wayapaychat.paymentgateway.common.enums.MerchantTransactionMode;
import com.wayapaychat.paymentgateway.common.utils.PaymentGateWayCommonUtils;
import com.wayapaychat.paymentgateway.dao.TransactionSettlementDAO;
import com.wayapaychat.paymentgateway.dao.WayaPaymentDAO;
import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.entity.SandboxTransactionSettlement;
import com.wayapaychat.paymentgateway.entity.TransactionSettlement;
import com.wayapaychat.paymentgateway.enumm.MerchantPermissions;
import com.wayapaychat.paymentgateway.enumm.SettlementStatus;
import com.wayapaychat.paymentgateway.pojo.RolePermissionResponsePayload;
import com.wayapaychat.paymentgateway.pojo.RoleResponse;
import com.wayapaychat.paymentgateway.pojo.SettlementStatusUpdateDto;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentListResponse;
import com.wayapaychat.paymentgateway.pojo.waya.SettlementQueryPojo;
import com.wayapaychat.paymentgateway.pojo.waya.SuccessResponse;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantData;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantResponse;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionSettlementStats;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionSettlementsResponse;
import com.wayapaychat.paymentgateway.proxy.IdentityManagementServiceProxy;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import com.wayapaychat.paymentgateway.repository.SandboxPaymentGatewayRepository;
import com.wayapaychat.paymentgateway.repository.SandboxTransactionSettlementRepository;
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

    private SandboxTransactionSettlementRepository sandboxTransactionSettlementRepository;
    private PaymentGatewayRepository paymentGatewayRepository;

    private SandboxPaymentGatewayRepository sandboxPaymentGatewayRepository;
    private final IdentityManagementServiceProxy identityManagementServiceProxy;
    private final PaymentGateWayCommonUtils paymentGateWayCommonUtils;
    private TransactionSettlementDAO transactionSettlementDAO;

    private MerchantProxy merchantProxy;
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

        TransactionSettlementsResponse data = transactionSettlementDAO.merchantTransactionSettlementStats(merchantIdToUse, mode);
            return new ResponseEntity<>(new SuccessResponse("Data successfully fetched", data), HttpStatus.OK);
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
        MerchantData merchantData = merchantResponse.getData();
        String mode = merchantData.getMerchantKeyMode();
        Page<TransactionSettlement> transactionSettlement;
        Page<SandboxTransactionSettlement> sandboxTransactionSettlement;
//        RolePermissionResponsePayload response = roleProxy.fetchUserRoleAndPermissions(merchantResponse.getData().getUserId(), token);
//        if (response.getPermissions().contains(MerchantPermissions.CAN_VIEW_SETTLEMENTS)) {
        if(mode.equals(MerchantTransactionMode.PRODUCTION.name())) {
            transactionSettlement = transactionSettlementRepository.findAll(merchantId, pageable);
            return new ResponseEntity<>(new SuccessResponse("Data successfully fetched", transactionSettlement), HttpStatus.OK);
        }else{
            sandboxTransactionSettlement = sandboxTransactionSettlementRepository.findAll(merchantId, pageable);
            return new ResponseEntity<>(new SuccessResponse("Data successfully fetched", sandboxTransactionSettlement), HttpStatus.OK);
        }
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
    public PaymentGatewayResponse updateMerchantSettlement(SettlementStatusUpdateDto settlementStatusUpdateDto) {

        List<PaymentGateway> allPayments = paymentGatewayRepository.findTransactionsByMerchantAndSettlementStatus(settlementStatusUpdateDto.getMerchantId());
        allPayments.stream().forEach( paymentGateway -> {
                paymentGateway.setSettlementStatus(SettlementStatus.SETTLED);
        });
        return new PaymentGatewayResponse(true, "Updated Successfully", null);
    }

    @Override
    public PaymentListResponse fetchAllMerchantTransactionsPendingSettlement(String merchantId) {
        List<PaymentGateway> allPayments = paymentGatewayRepository.getAllTransactionNotSettled(merchantId);
        allPayments.stream().forEach(payment -> payment.setSentForSettlement(Boolean.TRUE));
        return new PaymentListResponse(true, "Retrieved Successfully", allPayments);
    }
}
