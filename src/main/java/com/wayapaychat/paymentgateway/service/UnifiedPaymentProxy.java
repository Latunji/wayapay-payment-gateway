package com.wayapaychat.paymentgateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.entity.PaymentWallet;
import com.wayapaychat.paymentgateway.enumm.PaymentChannel;
import com.wayapaychat.paymentgateway.enumm.TStatus;
import com.wayapaychat.paymentgateway.enumm.TransactionSettled;
import com.wayapaychat.paymentgateway.exception.CustomException;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.*;
import com.wayapaychat.paymentgateway.pojo.waya.*;
import com.wayapaychat.paymentgateway.proxy.QRCodeProxy;
import com.wayapaychat.paymentgateway.proxy.UnifiedPaymentApiClient;
import com.wayapaychat.paymentgateway.proxy.WalletProxy;
import com.wayapaychat.paymentgateway.repository.PaymentWalletRepository;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;

@Service
@Slf4j
public class UnifiedPaymentProxy {

    private static final String Mode = "AES/CBC/PKCS5Padding";
    private static SecretKeySpec secretKey;
    private static byte[] key;
    @Autowired
    WalletProxy wallProxy;
    @Autowired
    QRCodeProxy qrCodeProxy;
    @Autowired
    PaymentWalletRepository paymentWalletRepo;
    @Autowired
    UnifiedPaymentApiClient unifiedClient;
    @Value("${waya.unified-payment.merchant}")
    private String merchantId;
    @Value("${waya.unified-payment.secret}")
    private String merchantSecret;
    @Value("${waya.unified-payment.baseurl}")
    private String merchantUrl;
    @Value("${waya.callback.baseurl}")
    private String callbackUrl;

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
            if (tmp.length() == 1) {
                sb.append("0");
            }
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

    public static String encrypt(String content, String password) {
        try {
            log.info("ENCRYPT PASSWORD: " + password);
            log.info("ENCRYPT CONTENT: " + content);
            byte[] data = content.getBytes();
            byte[] keybytes = password.substring(0, 16).getBytes();
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

    public String postUnified(WayaPaymentRequest payment) {
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

    public String getPaymentStatus(String tranId, String encryptData) {
        String Response = null;
        String homeDirectory = System.getProperty("os.name");
        log.info("User Home= " + homeDirectory);
        String baseUrl = merchantUrl + "/Home/TransactionPost/" + tranId;
        UriComponentsBuilder builderURL = UriComponentsBuilder.fromHttpUrl(baseUrl).queryParam("mid", merchantId)
                .queryParam("payload", encryptData);
        log.info("PAYMENT URL= " + builderURL.toUriString());
        Response = builderURL.toUriString();
        return Response;
    }

    public String encryptPaymentDataAccess(UnifiedCardRequest card) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            card.setSecretKey(merchantSecret);
            @NotNull final String json = mapper.writeValueAsString(card);
            log.info("-----||||JSON {}||||-----", json);
            @NotNull final String key = sha1(merchantSecret).toLowerCase();
            @NotNull final String ENCRYPTED_STRING = encrypt(json, key);
            log.info("-----||||ENCRYPTED CARD REQUEST BODY {}||||----- " + ENCRYPTED_STRING);
            return ENCRYPTED_STRING;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public WayaTransactionQuery transactionQuery(String tranId) {
        HttpHeaders headers = new HttpHeaders();
        String baseUrl = merchantUrl + "/Status/" + tranId;
        UriComponentsBuilder builderURL = UriComponentsBuilder.fromHttpUrl(baseUrl);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<WayaTransactionQuery> resp = restTemplate.exchange(builderURL.toUriString(), HttpMethod.GET,
                entity, WayaTransactionQuery.class);
        return resp.getBody();
    }

    public WayaTransactionQuery postPayAttitude(WayaPayattitude pay) {
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

    public FundEventResponse postWalletTransaction(WayaWalletPayment account, String token, PaymentGateway mPay) {

        FundEventResponse result = null;
        WalletEventPayment event = new WalletEventPayment();
        event.setAmount(mPay.getAmount());
        event.setEventId("WAYAPAY");
        event.setTranCrncy("NGN");
        event.setCustomerAccountNumber(account.getAccountNo());
        event.setPaymentReference(mPay.getRefNo());
        String tranParticular = mPay.getDescription() + "-" + mPay.getRefNo();
        event.setTranNarration(tranParticular);
        event.setTransactionCategory("WITHDRAW");
        log.info("EVENT DEBIT: " + event);
        try {
            WalletPaymentResponse wallet = wallProxy.fundWayaAccount(token, event);
            log.info(wallet.toString());
            if (wallet.getStatus()) {
                log.info("FUNDING WALLET");
                for (FundEventResponse response : wallet.getData()) {
                    if (response.getPartTranType().equals("C")) {
                        result = response;
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

    public FundEventResponse postTransactionPosition(String token, PaymentGateway mPay) {

        FundEventResponse result = null;
        WalletOfficePayment event = new WalletOfficePayment();
        event.setAmount(mPay.getAmount());
        event.setCreditEventId("WAYAPAY");
        event.setTranCrncy("NGN");
        if (mPay.getChannel().compareTo(PaymentChannel.CARD) == 0) {
            event.setDebitEventId("UNIPAY");
        } else if (mPay.getChannel().compareTo(PaymentChannel.PAYATTITUDE) == 0) {
            event.setDebitEventId("UNIPAY");
        } else if (mPay.getChannel().compareTo(PaymentChannel.USSD) == 0) {
            event.setDebitEventId("CORAPAY");
        }
        event.setPaymentReference(mPay.getRefNo());
        String tranParticular = mPay.getDescription() + "-" + mPay.getRefNo();
        event.setTranNarration(tranParticular);
        event.setTransactionCategory("WITHDRAW");
        log.info("EVENT DEBIT: " + event);
        try {
            PaymentWallet mWallet = new PaymentWallet();
            WalletPaymentResponse wallet = wallProxy.fundOfficialAccount(token, event);
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

    public WalletQRResponse postQRTransaction(PaymentGateway account, String token, WayaQRRequest request) {
        WalletQRResponse wallet = new WalletQRResponse();
        WalletQRGenerate qrgen = new WalletQRGenerate();
        qrgen.setPayableAmount(account.getAmount());
        qrgen.setActive(true);
        qrgen.setCustomerSessionId(account.getRefNo());
        qrgen.setMerchantId(account.getMerchantId());
        qrgen.setPaymentChannel("QR");
        String tranParticular = account.getDescription() + "-" + account.getRefNo();
        qrgen.setTransactionNarration(tranParticular);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String expiryDate = sdf.format(request.getQrExpiryDate());
        log.info("QR TIME: " + expiryDate);
        qrgen.setQrCodeExpiryDate(expiryDate);
        qrgen.setUserId(0);
        log.info("Request QR: " + qrgen);
        try {
            wallet = qrCodeProxy.wayaQRGenerate(qrgen);
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
}
