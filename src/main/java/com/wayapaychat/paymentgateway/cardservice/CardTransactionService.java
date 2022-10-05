package com.wayapaychat.paymentgateway.cardservice;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayapaychat.paymentgateway.cardservice.cardacquiring.CardAcqService;
import com.wayapaychat.paymentgateway.cardservice.exceptions.NotFoundException;
import com.wayapaychat.paymentgateway.common.utils.PaymentGateWayCommonUtils;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantData;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantResponse;
import com.wayapaychat.paymentgateway.service.impl.MerchantProxy;
import com.wayapaychat.paymentgateway.utility.AES;
import com.wayapaychat.paymentgateway.utility.CustomResponseCode;
import com.wayapaychat.paymentgateway.utility.Utility;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@SuppressWarnings("ALL")
@Service
@Slf4j
public class CardTransactionService {


    @Autowired
    private MerchantProxy merchantProxy;
    @Autowired
    private PaymentGateWayCommonUtils paymentGateWayCommonUtils;
    @Autowired
    private CardAcqService cardAcqService;


    public CardTransactionResponse cardPayment(EncryptedCardRequest request) throws IOException {
//        transactionValidations.validatePayment(request);

        MerchantResponse merchant = null;
        String token = paymentGateWayCommonUtils.getDaemonAuthToken();
        // get merchant data
        merchant = merchantProxy.getMerchantInfo(token, request.getMerchantId());
        if (merchant == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, "Profile doesn't exist");
        }
        if (!merchant.getCode().equals("00")) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, "Merchant id doesn't exist");
        }
        MerchantData sMerchant = merchant.getData();

        CardTransactionRequest result = new CardTransactionRequest();
        String encryptedString = request.getData();
        String decrypt = AES.decrypt(encryptedString, merchant.getData().getMerchantSecretKey());
        String response = decrypt;
        ObjectMapper objectMapper = new ObjectMapper();
        result = objectMapper.readValue(response, CardTransactionRequest.class);

        CardPaymentRequest cardPaymentRequest = new CardPaymentRequest();
        cardPaymentRequest.setMerchantId(request.getMerchantId());
        cardPaymentRequest.setMerchantName(request.getMerchantId());
        cardPaymentRequest.setCardNumber(result.getCardNumber());
        cardPaymentRequest.setExpirationMonth(result.getExpirationMonth());
        cardPaymentRequest.setExpirationYear(result.getExpirationYear());
        cardPaymentRequest.setAmount(result.getAmount());
        cardPaymentRequest.setCurrency(result.getCurrency());
        cardPaymentRequest.setSecurityCode(result.getSecurityCode());
        cardPaymentRequest.setCustomerId(result.getEmail());
        cardPaymentRequest.setTransactionId(Utility.transactionId());
        cardPaymentRequest.setMode(request.getMode());

        CardTransactionResponse response1 = cardAcqService.cardPayment(cardPaymentRequest);
        return response1;
    }




}
