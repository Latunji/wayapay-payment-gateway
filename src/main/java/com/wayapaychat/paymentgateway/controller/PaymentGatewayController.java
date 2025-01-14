package com.wayapaychat.paymentgateway.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayapaychat.paymentgateway.common.utils.PageableResponseUtil;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.*;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDPayment;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDRequest;
import com.wayapaychat.paymentgateway.pojo.waya.*;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.*;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import com.wayapaychat.paymentgateway.service.PaymentGatewayService;
import com.wayapaychat.paymentgateway.service.impl.UnifiedPaymentProxy;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

@CrossOrigin
@RestController
@RequestMapping("/api/v1")
@Tag(name = "PAYMENT-GATEWAY", description = "Payment Gateway User Service API")
@Validated
@Slf4j
public class PaymentGatewayController {

    @Autowired
    PaymentGatewayService paymentGatewayService;
    @Autowired
    UnifiedPaymentProxy unifiedPayment;
    @Autowired
    PaymentGatewayRepository paymentGatewayRepo;
    @Value("${service.thirdparty.unified-payment.callback.accepted-origins}")
    private String acceptedUnifiedPaymentCallbackOrigins;

    @ApiOperation(value = "QR-Code Waya-Request Transaction", notes = "This endpoint create client user", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/generate/qr-code")
    public ResponseEntity<?> generateQRCode(HttpServletRequest request, @Valid @RequestBody WayaQRRequest account) {
        return paymentGatewayService.walletPaymentQR(request, account);
    }

    @ApiOperation(value = "USSD Waya-Request Transaction", notes = "This endpoint create client user", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/request/ussd")
    public ResponseEntity<?> initiateUSSDTransaction(HttpServletRequest request, @Valid @RequestBody WayaUSSDRequest account) {
        return paymentGatewayService.initiateUSSDTransaction(request, account);
    }

    @ApiOperation(value = "USSD Waya-Payment Transaction", notes = "This stores the transaction status from USSD", tags = {"PAYMENT-GATEWAY"})
    @PutMapping("/payment/ussd/{refNo}")
    public ResponseEntity<?> updateUSSDTransaction(HttpServletRequest request, @Valid @RequestBody WayaUSSDPayment account,
            @PathVariable("refNo") final String refNo) {
        return paymentGatewayService.updateUSSDTransaction(request, account, refNo);
    }

    @ApiOperation(value = "Wallet Waya-Request Transaction", notes = "This endpoint create client user", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/request/wallet")
    public ResponseEntity<?> initiateWalletPayment(HttpServletRequest request,
            @Valid @RequestBody WayaWalletRequest account) {
        return paymentGatewayService.initiateWalletPayment(request, account);
    }

    @ApiOperation(value = "Wallet Authentication", notes = "This endpoint create client user", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/authentication/wallet")
    public ResponseEntity<?> walletAuthentication(HttpServletRequest request,
            @Valid @RequestBody WayaAuthenicationRequest account) {
        return paymentGatewayService.walletAuthentication(request, account);
    }

    @ApiOperation(value = "Wallet Waya-Payment Processing", notes = "This endpoint create client user", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/wallet/payment")
    public ResponseEntity<?> processWalletPayment(HttpServletRequest request,
            @Valid @RequestBody WayaWalletPayment payment, @RequestHeader("Authorization") String token) {
        return paymentGatewayService.processWalletPayment(request, payment, token);

    }

    @ApiOperation(value = "Get Transaction Status", notes = "This endpoint transaction status", tags = {"PAYMENT-GATEWAY"})
    @GetMapping("/wallet/query/{tranId}")
    public ResponseEntity<?> getWalletTransaction(HttpServletRequest request,
            @PathVariable("tranId") final String tranId) {
        return paymentGatewayService.getTransactionStatus(request, tranId);
    }

    @ApiOperation(value = "Get Merchant Account Numbers", notes = "This endpoint fetch merchant account no", tags = {"PAYMENT-GATEWAY"})
    @GetMapping("/wallet/accounts/{merchantId}")
    public PaymentGatewayResponse getMerchantAccountNumbers(@RequestHeader("authorization") String token,
            @PathVariable("merchantId") final String merchantId) {
        return paymentGatewayService.getMerchantAccounts(token, merchantId);
    }

    @ApiOperation(value = "Get Wallet Balance", notes = "This endpoint gets merchant wallet balance", tags = {"PAYMENT-GATEWAY"})
    @GetMapping("/wallet/balance/{merchantId}")
    public ResponseEntity<?> getWalletBalance(HttpServletRequest request, @PathVariable("merchantId") final String merchantId,
            @RequestHeader("Authorization") String token) throws JsonProcessingException {
      return paymentGatewayService.getWalletBalance(request, merchantId, token);

    }

    @ApiOperation(value = "Withdraw From Wallet", notes = "This endpoint allows merchant withdraw from wallet balance", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/wallet/withdrawal")
    public PaymentGatewayResponse withdrawFromWallet(HttpServletRequest request, @RequestBody WayaWalletWithdrawal walletPayment,
            @RequestHeader("Authorization") String token) throws JsonProcessingException {
        return paymentGatewayService.withdrawFromWallet(request, walletPayment, token);
    }

    @ApiOperation(value = "Withdrawal Stats", notes = "This endpoint allows merchant to view withdrawal stats", tags = {"PAYMENT-GATEWAY"})
    @GetMapping("/wallet/withdrawal/stats/{merchantId}")
    public PaymentGatewayResponse withdrawWalletStats(HttpServletRequest request, @PathVariable("merchantId") String merchantId,
                                                     @RequestHeader("Authorization") String token) throws JsonProcessingException {
        return paymentGatewayService.withdrawStats(request, merchantId, token);
    }

    @ApiOperation(value = "Withdrawal History", notes = "This endpoint allows merchant to view withdrawal history", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/wallet/withdrawal/history")
    public PaymentGatewayResponse withdrawalHistory(HttpServletRequest request, @RequestParam("merchantId") String merchantId,
                                                      @RequestHeader("Authorization") String token, @RequestBody PaginationPojo paginationPojo) throws JsonProcessingException {
        return paymentGatewayService.withdrawalHistory(request, merchantId, token, PageableResponseUtil.createPageRequest(paginationPojo.getPage(),
                paginationPojo.getSize(), paginationPojo.getOrder(),
                paginationPojo.getSortBy(), true, "withdrawal_date"
        ));
    }

    @ApiOperation(value = "Admin Withdraw For Merchant", notes = "This endpoint allows admin withdraw from merchant wallet balance", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/admin/wallet/withdrawal")
    public ResponseEntity<?> withdrawFromWalletAdmin(HttpServletRequest request, @RequestBody AdminWayaWithdrawal walletPayment,
            @RequestHeader("Authorization") String token) throws JsonProcessingException {
        PaymentGatewayResponse resp = paymentGatewayService.adminWithdrawFromWallet(request, walletPayment, token);
        if (!resp.getStatus()) {
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @ApiOperation(value = "Waya-Request Transaction", notes = "This endpoint create client user", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/request/transaction")
    public ResponseEntity<?> initiateTransaction(HttpServletRequest request, Device device,
            @Valid @RequestBody WayaPaymentRequest account) throws JsonProcessingException {
        PaymentGatewayResponse resp = paymentGatewayService.initiateCardTransaction(request, account, device);
        if (!resp.getStatus()) {
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @ApiOperation(value = "Waya-Payment Card Processing", notes = "This endpoint create client user", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/transaction/payment")
    public ResponseEntity<?> processPaymentWithCard(HttpServletRequest request, @Valid @RequestBody WayaCardPayment card) throws JsonProcessingException {
        ResponseEntity<?> resp = paymentGatewayService.processPaymentWithCard(request, card);
        return new ResponseEntity<>(resp, HttpStatus.OK);

    }

    @ApiOperation(value = "Request Callback URL", notes = "This endpoint create client user", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/transaction/processing")
    public ResponseEntity<?> processCardTransaction(HttpServletRequest request, HttpServletResponse response,
            @Valid @RequestBody WayaPaymentCallback pay) {
        PaymentGatewayResponse resp = paymentGatewayService.processCardTransaction(request, response, pay);
        if (!resp.getStatus()) {
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(resp, HttpStatus.OK);

    }

    @ApiOperation(value = "Request Callback URL", notes = "This endpoint create client user", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/transaction/processing/bank")
    public ResponseEntity<?> payAttitudeCallback(HttpServletRequest request,
            @Valid @RequestBody WayaPaymentCallback pay) {
        PaymentGatewayResponse resp = paymentGatewayService.payAttitudeCallback(request, pay);
        if (!resp.getStatus()) {
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(resp, HttpStatus.OK);

    }

    //TODO: validate that the request is from third party
    //TODO: this seems to be vulnerable as anybody can send a post request to update payment status
    //TODO: secrete key can be used to validate that the request came from the right source
//    @CrossOrigin(origins = {""})
    @PostMapping(value = "/wayaCallBack", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiOperation(value = "Waya Callback URL", notes = "This endpoint create client user", tags = {"PAYMENT-GATEWAY"})
    public ResponseEntity<?> upPaymentCallback(WayaCallbackRequest requests, HttpServletRequest httpServletRequest) throws MalformedURLException, URISyntaxException {
        CompletableFuture.runAsync(() -> {
            log.info("{}", httpServletRequest);
            log.info("REMOTE ADDRESS POSTED CALLBACK FOR PAYMENT ::: " + httpServletRequest.getRemoteAddr());
            log.info("REMOTE HOST POSTED CALLBACK FOR PAYMENT ::: " + httpServletRequest.getRemoteAddr());
            log.info("CALLBACK REQUEST ::: " + requests);
        });
        return paymentGatewayService.updatePaymentStatus(requests, httpServletRequest.getHeader("Authorization"));
    }

    @ApiOperation(value = "Get Transaction Status", notes = "This endpoint transaction status", tags = {"PAYMENT-GATEWAY"})
    @GetMapping("/transaction/query/{tranId}")
    public ResponseEntity<?> getTransactionStatus(HttpServletRequest request,
            @PathVariable("tranId") final String tranId) {
        return paymentGatewayService.getTransactionStatus(request, tranId);
    }

    @ApiOperation(value = "Get Reference", notes = "This endpoint transaction status", tags = {"PAYMENT-GATEWAY"})
    @GetMapping("/reference/query/{refNo}")
    public ResponseEntity<?> getTransactionByRef(HttpServletRequest request, @PathVariable("refNo") final String refNo) {
        return paymentGatewayService.getTransactionByRef(request, refNo);
    }

    @ApiOperation(value = "Update Transaction status", notes = "This endpoint transaction status", tags = {"PAYMENT-GATEWAY"})
    @PutMapping("/transaction/abandon/{refNo}")
    public ResponseEntity<?> abandonTransaction(HttpServletRequest request, @PathVariable("refNo") final String refNo,
            @Valid @RequestBody WayaPaymentStatus pay) {
        return paymentGatewayService.abandonTransaction(request, refNo, pay);
    }

    @ApiOperation(value = "Update Transaction status", notes = "This endpoint transaction status", tags = {"PAYMENT-GATEWAY"})
    @PutMapping("/transaction/status/{refNo}")
    public ResponseEntity<?> updateTransactionStatus(HttpServletRequest request, @PathVariable("refNo") final String refNo) {
        return paymentGatewayService.updatePaymentStatus(refNo, request.getHeader("Authorization"));
    }

    @ApiOperation(value = "Card Encryption", notes = "This endpoint create client user", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/card/encryption")
    public ResponseEntity<?> encryptCard(HttpServletRequest request, @Valid @RequestBody WayaEncypt pay) {
        PaymentGatewayResponse resp = paymentGatewayService.encryptCard(request, pay);
        if (!resp.getStatus()) {
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(resp, HttpStatus.OK);

    }

    @ApiOperation(value = "Card Decryption", notes = "This endpoint create client user", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/card/decryption")
    public ResponseEntity<?> decryptCard(HttpServletRequest request, @Valid @RequestBody WayaDecypt pay) {
        PaymentGatewayResponse resp = paymentGatewayService.decryptCard(request, pay);
        if (!resp.getStatus()) {
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(resp, HttpStatus.OK);

    }

    @ApiOperation(value = "Get Transaction Status", notes = "This endpoint transaction status", tags = {"PAYMENT-GATEWAY"})
    @GetMapping("/report/query")
    public ResponseEntity<?> getQueryTransactionStatus(HttpServletRequest request) {
        return paymentGatewayService.queryTranStatus(request);
    }

    //TODO: protect this endpoint before request comes IN
    @ApiOperation(value = "Get Transaction Status", notes = "This endpoint transaction status", tags = {"PAYMENT-GATEWAY"})
    @GetMapping("/transaction/report")
    public ResponseEntity<?> getMerchantTransactionReport(
            HttpServletRequest request,
            @RequestParam(value = "merchantId", required = false) final String merchantId) {
        return paymentGatewayService.getMerchantTransactionReport(request, merchantId);
    }

    @ApiOperation(value = "Get all merchant transactions", notes = "This endpoint get all merchant transactions", tags = {"PAYMENT-GATEWAY"})
    @GetMapping("/report/query/{merchantId}")
    public ResponseEntity<?> getAllMerchantTransactionsByMerchantId(@PathVariable final String merchantId, @RequestHeader("Authorization") String token) {
        return paymentGatewayService.fetchAllMerchantTransactions(merchantId, token);
    }

    @ApiOperation(value = "Get Transaction Status", notes = "This endpoint transaction status", tags = {"PAYMENT-GATEWAY"})
    @GetMapping("/revenue/query/{merchantId}")
    public ResponseEntity<?> getMerchantTransactionRevenue(HttpServletRequest request, @PathVariable("merchantId") final String merchantId, @RequestHeader("Authorization") String token) {
        return paymentGatewayService.getMerchantTransactionRevenue(request, merchantId, token);
    }

    @ApiOperation(value = "Get All Revenue {ADMIN ONLY}", notes = "This endpoint transaction status", tags = {"PAYMENT-GATEWAY"})
    @GetMapping("/revenue/query")
    public ResponseEntity<?> getAllTransactionRevenue(HttpServletRequest request) {
        return paymentGatewayService.getAllTransactionRevenue(request);
    }

    @ApiOperation(value = "Card Tokenization", notes = "This endpoint is to tokenize a card", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/card/tokenize")
    public ResponseEntity<?> tokenizeCard(HttpServletRequest request, @Valid @RequestBody CardTokenization CardTokenization, @RequestHeader("Authorization") String token) {
        ResponseEntity<?> resp = paymentGatewayService.tokenizeCard(CardTokenization, token);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @ApiOperation(value = "Pay with token", notes = "This endpoint allows payment with token", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/card/tokenize-pay")
    public ResponseEntity<?> payWithToken(@RequestParam String customerId, @RequestParam String merchantId,
            @RequestParam String transactionRef, @RequestParam String cardToken,
            @RequestHeader("Authorization") String token) {
        ResponseEntity<?> resp = paymentGatewayService.tokenizePayment(customerId, merchantId, transactionRef,
                cardToken, token);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @ApiOperation(value = "Charge with token", notes = "This endpoint allows charge with token", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/card/charge")
    public ResponseEntity<?> chargeWithToken(@RequestParam String customerId, @RequestParam String amount,
            @RequestParam String transactionRef, @RequestParam String cardToken,
            @RequestHeader("Authorization") String token) {
        ResponseEntity<?> resp = paymentGatewayService.chargeWithToken(customerId, transactionRef,
                cardToken, amount, token);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
