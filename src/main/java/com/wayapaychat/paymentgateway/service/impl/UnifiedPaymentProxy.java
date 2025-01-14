package com.wayapaychat.paymentgateway.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayapaychat.paymentgateway.common.enums.Constant;
import com.wayapaychat.paymentgateway.common.enums.MerchantTransactionMode;
import com.wayapaychat.paymentgateway.config.FeignClientInterceptor;
import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.entity.PaymentWallet;
import com.wayapaychat.paymentgateway.entity.SandboxPaymentGateway;
import com.wayapaychat.paymentgateway.enumm.PaymentChannel;
import com.wayapaychat.paymentgateway.enumm.TStatus;
import com.wayapaychat.paymentgateway.enumm.TransactionSettled;
import com.wayapaychat.paymentgateway.exception.CustomException;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.*;
import com.wayapaychat.paymentgateway.pojo.waya.*;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.*;
import com.wayapaychat.paymentgateway.proxy.QRCodeProxy;
import com.wayapaychat.paymentgateway.proxy.UnifiedPaymentApiClient;
import com.wayapaychat.paymentgateway.proxy.WalletProxy;
import com.wayapaychat.paymentgateway.proxy.WithdrawalProxy;
import com.wayapaychat.paymentgateway.repository.PaymentWalletRepository;
import com.wayapaychat.paymentgateway.utility.Utility;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.NotNull;
import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class UnifiedPaymentProxy {

    private static final String Mode = "AES/CBC/PKCS5Padding";
    private static SecretKeySpec secretKey;
    private static byte[] key;
    private final ModelMapper modelMapper = new ModelMapper();
    @Autowired
    WalletProxy wallProxy;
    @Autowired
    WithdrawalProxy withdrawalProxy;
    @Autowired
    QRCodeProxy qrCodeProxy;
    @Autowired
    PaymentWalletRepository paymentWalletRepo;
//    @Autowired
//    UnifiedPaymentApiClient unifiedClient;
    @Value("${waya.callback.baseurl}")
    private String callbackUrl;
    @Value("${waya.unified-payment.liveMerchant}")
    private String liveMerchantId;
    @Value("${waya.unified-payment.liveSecret}")
    private String liveMerchantSecret;
    @Value("${waya.unified-payment.liveBaseurl}")
    private String liveMerchantUrl;
    @Value("${waya.unified-payment.testMerchant}")
    private String testMerchantId;
    @Value("${waya.unified-payment.testSecret}")
    private String testMerchantSecret;
    @Value("${waya.unified-payment.testBaseurl}")
    private String testMerchantUrl;

    private final Random rnd = new Random();
    private String merchantId;
    private String merchantSecret;
    private String merchantUrl;

    private void setVars(String mode) {
        if (mode.equals(MerchantTransactionMode.TEST.name())) {
            this.merchantId = testMerchantId;
            this.merchantSecret = testMerchantSecret;
            this.merchantUrl = testMerchantUrl;
        } else {
            this.merchantId = liveMerchantId;
            this.merchantSecret = liveMerchantSecret;
            this.merchantUrl = liveMerchantUrl;
        }
    }

    @SuppressWarnings("unused")
    private static byte[] fromHex(String inputString) {
        if (inputString == null || inputString.length() < 2) {
            return new byte[0];
        }
        inputString = inputString.toLowerCase();
        int l = inputString.length() / 2;
        byte[] result = new byte[l];
        for (int i = 0; i < l; ++i) {
            String tmp = inputString.substring(2 * i, 2 * i + 2);
            result[i] = (byte) (Integer.parseInt(tmp, 16) & 0xFF);
        }
        return result;
    }

    public static String toHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        String tmp = "";
        for (byte value : b) {
            tmp = (Integer.toHexString(value & 0XFF));
            if (tmp.length() == 1)
                sb.append("0");
            sb.append(tmp);
        }
        return sb.toString().toUpperCase();
    }

    public static String sha1(String input) {
        String sha1 = null;
        try {
            MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
            msdDigest.update(input.getBytes(StandardCharsets.UTF_8), 0, input.length());
            sha1 = DatatypeConverter.printHexBinary(msdDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sha1;
    }

    public static String encrypt(String content, String merchantSecretSha1) {
        try {
            log.info("ENCRYPT PASSWORD: " + merchantSecretSha1);
            log.info("ENCRYPT CONTENT: " + content);
            byte[] data = content.getBytes();
            byte[] keybytes = merchantSecretSha1.substring(0, 16).getBytes();
            Cipher cipher = Cipher.getInstance(Mode);
            SecretKeySpec spec = new SecretKeySpec(keybytes, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, spec, new IvParameterSpec(keybytes));
            return toHex(cipher.doFinal(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setKey(String myKey) {
        MessageDigest sha;
        try {
            key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String getDataEncrypt(String dataToEncrypt, String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(dataToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.getLocalizedMessage() + " : " + e.getMessage());
        }
        return null;
    }

    public static String getDataDecrypt(String strToDecrypt, String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e);
        }
        return "";
    }

    // considered
    public String postUnified(WayaPaymentRequest payment) {
        this.setVars(payment.getMode());
        log.info("USING : "+merchantUrl);
        try {
            log.info("Waya Payment Request: {}", payment.toString());
            UnifiedPaymentRequest uniRequest = new UnifiedPaymentRequest();
            uniRequest.setId(merchantId);
            uniRequest.setDescription(payment.getDescription());
            uniRequest.setAmount(payment.getAmount());
            uniRequest.setFee(payment.getFee());
            uniRequest.setCurrency(payment.getCurrency());
            uniRequest.setReturnUrl(callbackUrl);
            uniRequest.setSecretKey(merchantSecret);

            log.info("Unified Payment Request: {}", uniRequest);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = null;

            try {
                jsonString = mapper.writeValueAsString(uniRequest);
                log.info("ResultingJSONstring = " + jsonString);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            String baseUrl = merchantUrl + "/Aggregator";
            UriComponentsBuilder builderURL = UriComponentsBuilder.fromHttpUrl(baseUrl);
            log.info("URL= " + builderURL.toUriString());
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> entity = new HttpEntity<>(jsonString, headers);
            ResponseEntity<String> resp = restTemplate.exchange(builderURL.toUriString(), HttpMethod.POST, entity,
                    String.class);
            log.info("Return Message: " + resp.getBody());
            return resp.getBody();

        } catch (Exception ex) {
            log.error("Higher Wahala {}", ex.getMessage());
        }
        return null;
    }

    // considered
    public String buildUnifiedPaymentURLWithPayload(String tranId, String encryptData, boolean recurrent, String mode) {
        this.setVars(mode);
        log.info("USING : "+merchantUrl);
        String subPathURL = recurrent ?
                "/Home/RecurringTransaction/" : "/Home/TransactionPost/";
        UriComponentsBuilder builderURL = null;
        if (recurrent) {
            builderURL = UriComponentsBuilder
                    .fromHttpUrl(merchantUrl + subPathURL + merchantId);
        } else {
            builderURL = UriComponentsBuilder
                    .fromHttpUrl(merchantUrl + subPathURL + tranId)
                    .queryParam("mid", merchantId);
        }
        builderURL.queryParam("payload", encryptData);
        log.info("PAYMENT URL= " + builderURL.toUriString());
        return builderURL.toUriString();
    }

    // considered
    public String encryptPaymentDataAccess(UnifiedCardRequest card, String mode) throws JsonProcessingException {
        this.setVars(mode);
        log.info("USING : "+merchantUrl);
        Object objectToEncrypt = null;
        Map<String, Object> dataToEncrypt = new HashMap<>();
        dataToEncrypt.put("secretKey", merchantSecret);
        dataToEncrypt.put("scheme", card.getScheme());
        dataToEncrypt.put("cardHolder", card.getCardHolder());
        dataToEncrypt.put("cardNumber", card.getCardNumber());
        dataToEncrypt.put("cvv", card.getCvv());
        dataToEncrypt.put("expiry", card.getExpiry());
        dataToEncrypt.put("pin", card.getPin());
        dataToEncrypt.put("mobile", card.getMobile());
        if (card.isRecurring()) {
            UnifiedRecurrentCardRequest unifiedRecurrentCardRequest = new UnifiedRecurrentCardRequest();
            unifiedRecurrentCardRequest.setSecretKey(merchantSecret);
            unifiedRecurrentCardRequest.setScheme(card.getScheme());
            unifiedRecurrentCardRequest.setCardHolder(card.getCardHolder());
            unifiedRecurrentCardRequest.setCardNumber(card.getCardNumber());
            unifiedRecurrentCardRequest.setCvv(card.getCvv());
            unifiedRecurrentCardRequest.setExpiry(card.getExpiry());
            unifiedRecurrentCardRequest.setPin(card.getPin());
            unifiedRecurrentCardRequest.setMobile(card.getMobile());
            unifiedRecurrentCardRequest.setEndRecurr(card.getEndRecurr());
            unifiedRecurrentCardRequest.setFrequency(card.getFrequency());
            unifiedRecurrentCardRequest.setIsRecurring(card.isRecurring());
            unifiedRecurrentCardRequest.setOrderExpirationPeriod(0);
            objectToEncrypt = unifiedRecurrentCardRequest;
        } else {
            objectToEncrypt = dataToEncrypt;
        }
        return encryptUnifiedPaymentPayload(objectToEncrypt, mode);
    }

    // considered
    private String encryptUnifiedPaymentPayload(Object objectToEncrypt, String mode) throws JsonProcessingException {
        this.setVars(mode);
        log.info("USING : "+merchantUrl);
        ObjectMapper mapper = new ObjectMapper();
        @NotNull final String json = mapper.writeValueAsString(objectToEncrypt);
        log.info("-----||||JSON {}||||-----", json);
        @NotNull final String MERCHANT_SECRET_SHA1 = sha1(merchantSecret).toLowerCase();
        @NotNull final String ENCRYPTED_STRING = encrypt(json, MERCHANT_SECRET_SHA1);
        System.out.println(MERCHANT_SECRET_SHA1);
        log.info("-----||||ENCRYPTED CARD REQUEST BODY {}||||----- " + ENCRYPTED_STRING);
        return ENCRYPTED_STRING;
    }

    // considered
    public WayaTransactionQuery transactionQuery(String tranId, String mode) {
        this.setVars(mode);
        log.info("USING : "+merchantUrl);
        HttpHeaders headers = new HttpHeaders();
        String baseUrl = merchantUrl + "/Status/" + tranId;
        UriComponentsBuilder builderURL = UriComponentsBuilder.fromHttpUrl(baseUrl);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<WayaTransactionQuery> resp = restTemplate.exchange(builderURL.toUriString(), HttpMethod.GET,
                entity, WayaTransactionQuery.class);
        return resp.getBody();
    }

    // considered
    public WayaTransactionQuery postPayAttitude(WayaPayattitude pay, String mode) {
        this.setVars(mode);
        log.info("USING : "+merchantUrl);
        try {
            UnifiedPaymentCallback callReq = new UnifiedPaymentCallback(pay.getTranId(), merchantId,
                    pay.getCardEncrypt());
            log.info("Unified Payment Request: {}", callReq);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = null;

            try {
                jsonString = mapper.writeValueAsString(callReq);
                log.info("Result JSON = " + jsonString);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            String baseUrl = merchantUrl + "/Home/PayAttitudeTransactionPost";
            UriComponentsBuilder builderURL = UriComponentsBuilder.fromHttpUrl(baseUrl);
            log.info("BASE URL= " + builderURL.toUriString());
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> entity = new HttpEntity<>(jsonString, headers);
            ResponseEntity<WayaTransactionQuery> resp = restTemplate.exchange(builderURL.toUriString(), HttpMethod.POST,
                    entity, WayaTransactionQuery.class);
            log.info("Return Message: " + resp.getBody());
            return resp.getBody();
        } catch (Exception ex) {
            log.error("Higher Wahala {}", ex.getMessage());
        }
        return null;
    }

    public FundEventResponse postWalletTransaction(WayaWalletPayment account, String token, PaymentGateway mPay, Long userId) {

        FundEventResponse fundEventResponse = null;
        WithdrawalRequest withdrawalRequest = null;
        Date dte = new Date();
        String strLong = Utility.transactionId();
        DefaultWalletResponse defaultWalletResponse = wallProxy.getUserDefaultWalletAccount(token,  userId);

        log.info("Wallet Proxy Response :::::"+defaultWalletResponse.toString());

        withdrawalRequest.setAmount(mPay.getAmount().doubleValue());
        withdrawalRequest.setNarration("WayaQuick Credit To Customer's Account");
        withdrawalRequest.setBankCode(Constant.WAYAQUICK_BANK_CODE);
        withdrawalRequest.setBankName(Constant.WAYAQUICK_DISBURSEMENT_BANK_NAME);
        withdrawalRequest.setCrAccount(Constant.WAYAQUICK_DISBURSEMENT_ACCOUNT_NUMBER);
        withdrawalRequest.setCrAccountName(Constant.WAYAQUICK_DISBURSEMENT_ACCOUNT_NAME);
        withdrawalRequest.setSaveBen(false);
        withdrawalRequest.setTransactionPin(mPay.getTransactionPin());
        withdrawalRequest.setUserId(String.valueOf(userId));
        withdrawalRequest.setTransRef(strLong);
        if (defaultWalletResponse.getStatus() == true) {
            withdrawalRequest.setWalletAccountNo(defaultWalletResponse.getData().getAccountNo());
        }
        try{
           WithdrawalResponse resp = withdrawalProxy.withdrawFromWallet(token, withdrawalRequest);
            log.info("Withdrawal Proxy Response :::::"+resp.toString());
            if(resp != null){
            fundEventResponse.setTranId(strLong);
            fundEventResponse.setPaymentReference(strLong);
            fundEventResponse.setTranNarrate(withdrawalRequest.getNarration());
            fundEventResponse.setTranAmount(mPay.getAmount());
            fundEventResponse.setTranDate(String.valueOf(LocalDateTime.now()));
        return fundEventResponse;
        }
        } catch (Exception ex) {
            if (ex instanceof FeignException) {
                String httpStatus = Integer.toString(((FeignException) ex).status());
                log.error("Feign Exception Status {}", httpStatus);
            }
            log.error("Higher Wahala {}", ex.getMessage());
            log.error("WALLET TRANSACTION FAILED: " + ex.getLocalizedMessage());
            throw new CustomException("WALLET TRANSACTION FAILED: " + ex.getLocalizedMessage() + " with Merchant: "
                    + mPay.getMerchantId(), HttpStatus.BAD_REQUEST);
        }
        return fundEventResponse;
    }

    public FundEventResponse postTransactionPosition(String token, PaymentGateway mPay) {

        FundEventResponse result = null;
        WalletOfficePayment walletOfficePayment = new WalletOfficePayment();
        walletOfficePayment.setAmount(mPay.getAmount());
        walletOfficePayment.setCreditEventId("WAYAPAY");
        walletOfficePayment.setTranCrncy("NGN");
        if (mPay.getChannel().compareTo(PaymentChannel.CARD) == 0) {
            walletOfficePayment.setDebitEventId("UNIPAY");
        } else if (mPay.getChannel().compareTo(PaymentChannel.PAYATTITUDE) == 0) {
            walletOfficePayment.setDebitEventId("UNIPAY");
        } else if (mPay.getChannel().compareTo(PaymentChannel.USSD) == 0) {
            walletOfficePayment.setDebitEventId("CORAPAY");
        }
        walletOfficePayment.setPaymentReference(mPay.getRefNo());
        String tranParticular = mPay.getDescription() + "-" + mPay.getRefNo();
        walletOfficePayment.setTranNarration(tranParticular);
        walletOfficePayment.setTransactionCategory("WITHDRAW");
        log.info("WAYABANK WALLET SETTLEMENT: " + walletOfficePayment);
        try {
            PaymentWallet mWallet = new PaymentWallet();
            WalletPaymentResponse wallet = wallProxy.fundOfficialAccount(token, walletOfficePayment);
            log.info(wallet.toString());
            if (wallet.getStatus()) {
                log.info("FUNDING WALLET");
                for (FundEventResponse response : wallet.getData()) {
                    if (response.getPartTranType().equals("C")) {
                        result = response;
                        mWallet.setPaymentDescription(response.getTranNarrate());
                        mWallet.setPaymentReference(response.getPaymentReference());
                        mWallet.setTranAmount(response.getTranAmount());
                        mWallet.setTranDate(response.getTranDate());
                        mWallet.setTranId(response.getTranId());
                        mWallet.setRefNo(mPay.getRefNo());
                        mWallet.setSettled(TransactionSettled.NOT_SETTLED);
                        mWallet.setStatus(TStatus.APPROVED);
                        paymentWalletRepo.save(mWallet);
                    }
                }
            } else {
                log.error("WALLET TRANSACTION FAILED: " + wallet.getMessage() + " with Merchant: "
                        + mPay.getMerchantId());
                throw new CustomException(
                        "WALLET TRANSACTION FAILED: " + wallet.getMessage() + " with Merchant: " + mPay.getMerchantId(),
                        HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            if (ex instanceof FeignException) {
                String httpStatus = Integer.toString(((FeignException) ex).status());
                log.error("Feign Exception Status {}", httpStatus);
            }
            log.error("Higher Wahala {}", ex.getMessage());
            log.error("WALLET TRANSACTION FAILED: " + ex.getLocalizedMessage());
            throw new CustomException("WALLET TRANSACTION FAILED: " + ex.getLocalizedMessage() + " with Merchant: "
                    + mPay.getMerchantId(), HttpStatus.BAD_REQUEST);
        }
        return result;
    }

    public WalletQRResponse postQRTransaction(PaymentGateway account, String token, WayaQRRequest request, ProfileResponse profile) {
        WalletQRResponse wallet = new WalletQRResponse();
        WalletQRGenerate qrgen = new WalletQRGenerate();
        qrgen.setPayableAmount(account.getAmount());
        qrgen.setActive(true);
        qrgen.setCustomerSessionId(account.getRefNo());
        qrgen.setMerchantId(account.getMerchantId());
        qrgen.setPaymentChannel("QR");
        qrgen.setMerchantEmail(account.getMerchantEmail());
        qrgen.setFirstName(profile.getData().getFirstName());
        qrgen.setSurname(profile.getData().getSurname());
        String tranParticular = account.getDescription() + "-" + account.getRefNo();
        qrgen.setTransactionNarration(tranParticular);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String expiryDate = sdf.format(request.getQrExpiryDate());
        log.info("QR TIME: " + expiryDate);
        qrgen.setQrCodeExpiryDate(expiryDate);
        qrgen.setUserId(0);
        log.info("Request QR: " + qrgen);
        try {
            wallet = qrCodeProxy.wayaQRGenerate(FeignClientInterceptor.getBearerTokenHeader(), qrgen);
            if (wallet.getStatusCodeValue() != 200) {
                log.error("QR TRANSACTION FAILED: " + wallet.getStatusCode() + " with Merchant: "
                        + account.getMerchantId());
            }
        } catch (Exception ex) {
            if (ex instanceof FeignException) {
                String httpStatus = Integer.toString(((FeignException) ex).status());
                log.error("Feign Exception Status {}", httpStatus);
            }
            log.error("Higher Wahala {}", ex.getMessage());
            log.error("WALLET TRANSACTION: " + ex.getLocalizedMessage());
        }
        return wallet;
    }


    public WalletQRResponse sandboxPostQRTransaction(SandboxPaymentGateway account, String token, WayaQRRequest request, ProfileResponse profile) {
        WalletQRResponse wallet = new WalletQRResponse();
        WalletQRGenerate qrgen = new WalletQRGenerate();
        qrgen.setPayableAmount(account.getAmount());
        qrgen.setActive(true);
        qrgen.setCustomerSessionId(account.getRefNo());
        qrgen.setMerchantId(account.getMerchantId());
        qrgen.setPaymentChannel("QR");
        qrgen.setMerchantEmail(account.getMerchantEmail());
        qrgen.setFirstName(profile.getData().getFirstName());
        qrgen.setSurname(profile.getData().getSurname());
        String tranParticular = account.getDescription() + "-" + account.getRefNo();
        qrgen.setTransactionNarration(tranParticular);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String expiryDate = sdf.format(request.getQrExpiryDate());
        log.info("QR TIME: " + expiryDate);
        qrgen.setQrCodeExpiryDate(expiryDate);
        qrgen.setUserId(0);
        log.info("Request QR: " + qrgen);
        try {
            wallet = qrCodeProxy.wayaQRGenerate(FeignClientInterceptor.getBearerTokenHeader(), qrgen);
            if (wallet.getStatusCodeValue() != 200) {
                log.error("QR TRANSACTION FAILED: " + wallet.getStatusCode() + " with Merchant: "
                        + account.getMerchantId());
            }
        } catch (Exception ex) {
            if (ex instanceof FeignException) {
                String httpStatus = Integer.toString(((FeignException) ex).status());
                log.error("Feign Exception Status {}", httpStatus);
            }
            log.error("Higher Wahala {}", ex.getMessage());
            log.error("WALLET TRANSACTION: " + ex.getLocalizedMessage());
        }
        return wallet;
    }

    // considered
    public void recurrentTransaction(PaymentGateway paymentGateway, String mode) throws JsonProcessingException {
        this.setVars(mode);
        log.info("USING : "+merchantUrl);
        UnifiedPaymentRecurrentPaymentRequest unifiedPaymentRecurrentPaymentRequest = new UnifiedPaymentRecurrentPaymentRequest();
        unifiedPaymentRecurrentPaymentRequest.setAmount(paymentGateway.getAmount());
        unifiedPaymentRecurrentPaymentRequest.setSecretKey(merchantSecret);
        unifiedPaymentRecurrentPaymentRequest.setScheme(paymentGateway.getScheme().toLowerCase());
        unifiedPaymentRecurrentPaymentRequest.setFee(0);
        unifiedPaymentRecurrentPaymentRequest.setSessionId(paymentGateway.getSessionId());
        unifiedPaymentRecurrentPaymentRequest.setCurrency(paymentGateway.getCurrencyCode());
        unifiedPaymentRecurrentPaymentRequest.setCustomerEmail(paymentGateway.getCustomerEmail());
        unifiedPaymentRecurrentPaymentRequest.setCustomerName(paymentGateway.getCustomerName());
        unifiedPaymentRecurrentPaymentRequest.setDescription(paymentGateway.getDescription());
        unifiedPaymentRecurrentPaymentRequest.setReturnUrl(callbackUrl);
        @NotNull final String ENCRYPTED_DATA = encryptUnifiedPaymentPayload(unifiedPaymentRecurrentPaymentRequest, mode);
        @NotNull final String PAYLOAD_WITH_URL = buildUnifiedPaymentURLWithPayload(
                paymentGateway.getTranId(), ENCRYPTED_DATA,
                paymentGateway.getIsFromRecurrentPayment(),
                mode);
        log.info("----|||| RECURRENT_PAYMENT_URL ||||----\n{}", PAYLOAD_WITH_URL);
    }
}
