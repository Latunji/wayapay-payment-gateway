package com.wayapaychat.paymentgateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.entity.SandboxPaymentGateway;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.*;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDPayment;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDRequest;
import com.wayapaychat.paymentgateway.pojo.waya.CardTokenization;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.waya.QueryCustomerTransactionPojo;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.net.URISyntaxException;
import java.util.Date;

public interface PaymentGatewayService {
    ResponseEntity<?> walletPaymentQR(HttpServletRequest request, WayaQRRequest account);

    ResponseEntity<?> initiateWalletPayment(HttpServletRequest request, WayaWalletRequest account);

    ResponseEntity<?> updateUSSDTransaction(HttpServletRequest request, WayaUSSDPayment account, String refNo);

    ResponseEntity<?> initiateUSSDTransaction(HttpServletRequest request, WayaUSSDRequest account);

    ResponseEntity<?> walletAuthentication(HttpServletRequest request, WayaAuthenicationRequest account);

    ResponseEntity<?> processWalletPayment(HttpServletRequest request, WayaWalletPayment payment, String token);

    PaymentGatewayResponse initiateCardTransaction(HttpServletRequest request, WayaPaymentRequest account, Device device) throws JsonProcessingException;

    void preprocessRecurrentPayment(UnifiedCardRequest cardRequest, WayaCardPayment card, Object paymentGatewayData, String mode);

    ResponseEntity<?> processPaymentWithCard(HttpServletRequest request, WayaCardPayment card) throws JsonProcessingException;

    PaymentGatewayResponse processCardTransaction(HttpServletRequest request, HttpServletResponse response, WayaPaymentCallback pay);

    PaymentGatewayResponse payAttitudeCallback(HttpServletRequest request, WayaPaymentCallback pay);

    ResponseEntity<?> getTransactionStatus(HttpServletRequest req, String tranId);

    ResponseEntity<?> fetchAllMerchantTransactions(String merchantId, String token);

    PaymentGatewayResponse getWalletBalance(HttpServletRequest request, String merchantId, String token);

//    PaymentGatewayResponse withdrawFromWallet(HttpServletRequest request, WayaWalletWithdrawal wayaWalletWithdrawal, String token);

    ResponseEntity<?> getTransactionByRef(HttpServletRequest req, String refNo);

    WayaTransactionQuery getTransactionStatus(String tranId);

    PaymentGatewayResponse encryptCard(HttpServletRequest request, WayaEncypt pay);

    PaymentGatewayResponse decryptCard(HttpServletRequest request, WayaDecypt pay);

    ResponseEntity<?> queryTranStatus(HttpServletRequest req);

    ResponseEntity<?> getMerchantTransactionReport(HttpServletRequest req, String merchantId);

    ResponseEntity<?> abandonTransaction(HttpServletRequest request, final String refNo, WayaPaymentStatus pay);

    ResponseEntity<?> getMerchantTransactionRevenue(HttpServletRequest req, String merchantId, String token);

    ResponseEntity<?> getAllTransactionRevenue(HttpServletRequest req);

    ResponseEntity<?> updatePaymentStatus(WayaCallbackRequest wayaCallbackRequest) throws URISyntaxException;

    ResponseEntity<?> updatePaymentStatus(String refNo);

    ResponseEntity<PaymentGatewayResponse> filterSearchCustomerTransactions(QueryCustomerTransactionPojo queryCustomerTransactionPojo, Pageable pageable);

    Page<?> getCustomerTransaction(QueryCustomerTransactionPojo queryPojo, String mode, Pageable pageable);

    void updateRecurrentTransaction(@NotNull PaymentGateway paymentGateway);

    void updateSandboxRecurrentTransaction(@NotNull SandboxPaymentGateway paymentGateway);

    ResponseEntity<PaymentGatewayResponse> getYearMonthTransactionStats(String merchantId, Long year, Date startDate, Date endDate, String token);

    ResponseEntity<PaymentGatewayResponse> getTransactionOverviewStats(@NotNull final String merchantId, String token);

    ResponseEntity<PaymentGatewayResponse> getTransactionGrossAndNetRevenue(String merchantId, String token);

    ResponseEntity<PaymentGatewayResponse> fetchPaymentLinkTransactions(String merchantId, String paymentLinkId, String token, Pageable pageable);

    ResponseEntity<?> tokenizeCard(CardTokenization cardTokenization, String token);

    ResponseEntity<?> tokenizePayment(String customerId, String merchantId, String transactionRef, String cardToken, String token);
}
