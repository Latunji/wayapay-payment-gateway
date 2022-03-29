package com.wayapaychat.paymentgateway.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayapaychat.paymentgateway.pojo.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.*;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDPayment;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDRequest;
import com.wayapaychat.paymentgateway.pojo.waya.*;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import com.wayapaychat.paymentgateway.service.PaymentGatewayService;
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
    PaymentGatewayRepository paymentGatewayRepo;
    @Value("${service.thirdparty.unified-payment.callback.accepted-origins}")
    private String acceptedUnifiedPaymentCallbackOrigins;

    @ApiOperation(value = "QR-Code Waya-Request Transaction", notes = "This endpoint create client user", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/generate/qr-code")
    public ResponseEntity<?> generateQRCode(HttpServletRequest request, @Valid @RequestBody WayaQRRequest account) {
        return paymentGatewayService.WalletPaymentQR(request, account);
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

    @ApiOperation(value = "Waya-Request Transaction", notes = "This endpoint create client user", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/request/transaction")
    public ResponseEntity<?> initiateTransaction(HttpServletRequest request, Device device,
                                                 @Valid @RequestBody WayaPaymentRequest account) throws JsonProcessingException {
        PaymentGatewayResponse resp = paymentGatewayService.initiateTransaction(request, account, device);
        if (!resp.getStatus())
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @ApiOperation(value = "Waya-Payment Card Processing", notes = "This endpoint create client user", tags = {"PAYMENT-GATEWAY"})
    @PostMapping("/transaction/payment")
    public ResponseEntity<?> processPaymentWithCard(HttpServletRequest request, @Valid @RequestBody WayaCardPayment card) {
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
        return paymentGatewayService.updatePaymentStatus(requests);
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
    public ResponseEntity<?> updateTransactionStatus(@PathVariable("refNo") final String refNo) {
        return paymentGatewayService.updateTransactionStatus(refNo);
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
    @GetMapping("/report/query/{merchantId}")
    public ResponseEntity<?> getMerchantTransactionReport(
            HttpServletRequest request,
            @PathVariable(value = "merchantId", required = false) final String merchantId) {
        return paymentGatewayService.getMerchantTransactionReport(request, merchantId);
    }

    //TODO: Protect this endpoint
    @ApiOperation(value = "Get Transaction Status", notes = "This endpoint transaction status", tags = {"PAYMENT-GATEWAY"})
    @GetMapping("/revenue/query/{merchantId}")
    public ResponseEntity<?> getMerchantTransactionRevenue(HttpServletRequest request,
                                                           @PathVariable("merchantId") final String merchantId) {
        return paymentGatewayService.getMerchantTransactionRevenue(request, merchantId);
    }

    @ApiOperation(value = "Get All Revenue", notes = "This endpoint transaction status", tags = {"PAYMENT-GATEWAY"})
    @GetMapping("/revenue/query")
    public ResponseEntity<?> getAllTransactionRevenue(HttpServletRequest request) {
        return paymentGatewayService.getAllTransactionRevenue(request);
    }

}
