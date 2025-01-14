package com.wayapaychat.paymentgateway.service;


import com.wayapaychat.paymentgateway.pojo.SettlementStatusUpdateDto;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentListResponse;
import com.wayapaychat.paymentgateway.pojo.waya.SettlementQueryPojo;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface TransactionSettlementService {
    // s-l done
    ResponseEntity<PaymentGatewayResponse> getSettlementStats(String merchantId);

    ResponseEntity<PaymentGatewayResponse> getCumulativeTransactionSettlement(SettlementQueryPojo settlementQueryPojo, String merchantId, Pageable dateSettled);

    ResponseEntity<PaymentGatewayResponse> getAllSettledSuccessfulTransactions(SettlementQueryPojo settlementQueryPojo, String merchantId, Pageable pageable);

    ResponseEntity<PaymentGatewayResponse> getSettlementByReferenceId(String settlementReferenceId);

    PaymentGatewayResponse fetchAllTransactionsPendingSettlement();

    PaymentGatewayResponse updateMerchantSettlement(SettlementStatusUpdateDto settlementStatusUpdateDto);

    PaymentListResponse fetchAllMerchantTransactionsPendingSettlement(String merchantId);
}
