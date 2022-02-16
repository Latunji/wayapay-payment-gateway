package com.wayapaychat.paymentgateway.service;

//import java.awt.Desktop;
//import java.io.IOException;
import java.io.UnsupportedEncodingException;
//import java.net.URI;
//import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.UnifiedCardRequest;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.UnifiedPaymentCallback;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.UnifiedPaymentRequest;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaPaymentCallback;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaPaymentRequest;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaTransactionQuery;
import com.wayapaychat.paymentgateway.proxy.UnifiedPaymentApiClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UnifiedPaymentProxy {

	@Value("${waya.unified-payment.merchant}")
	private String merchantId;

	@Value("${waya.unified-payment.secret}")
	private String merchantSecret;

	@Value("${waya.unified-payment.baseurl}")
	private String merchantUrl;
	
	@Value("${waya.callback.baseurl}")
	private String callbackUrl;

	private static final String Mode = "AES/CBC/PKCS5Padding";
	
	private static SecretKeySpec secretKey;
	
    private static byte[] key;

	@Autowired
	UnifiedPaymentApiClient unifiedClient;

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

			log.info("Unified Payment Request: {}", uniRequest.toString());

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
		// String encryptData =
		// "7180D90DBB64FF398285CB610812DDF2B427080CC37F32030A5A2C5032D47BD8E228499CE9269D40C42CDD1E1365FDA6C4F79A6EC74799F44527DF361C15A00090061AA7EBB95A9AF16DF9004B6726441567BD1C0D2380CE42FA43A43537AFE0D4E2D2071E22EA9CE070175E664B1A30A7216521FA4C5B6AE901C6D792F952FECCC2F9448496C0C701E3B58B5D51CAF2B99B9BD7DEAE2CC96206CCF611E579F1C52590E2C3E5F21DC4623F83B847B192045AB138B804F6311B3F8BA9AE03F5A8";
		String baseUrl = merchantUrl + "/Home/TransactionPost/" + tranId;
		UriComponentsBuilder builderURL = UriComponentsBuilder.fromHttpUrl(baseUrl).queryParam("mid", merchantId)
				.queryParam("payload", encryptData);
		log.info("PAYMENT URL= " + builderURL.toUriString());
		Response = builderURL.toUriString();
		/*Runtime runtime = Runtime.getRuntime();
		try {
			if (homeDirectory.contains("window")) {
				URI homepage = new URI(builderURL.toUriString());
				Desktop.getDesktop().browse(homepage);
			} else {
				runtime.exec("rundll32 url.dll,FileProtocolHandler " + builderURL.toUriString());
				Thread.sleep(5000);
			}
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}*/
		return Response;
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
		StringBuffer sb = new StringBuffer(b.length * 2);
		String tmp = "";
		for (int n = 0; n < b.length; n++) {
			tmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
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
			msdDigest.update(input.getBytes("UTF-8"), 0, input.length());
			sha1 = DatatypeConverter.printHexBinary(msdDigest.digest());
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return sha1;
	}

	public static String encrypt(String content, String password) {
		try {
			log.info("ENCRYPT PASSWORD: " +password);
			log.info("ENCRYPT CONTENT: " + content);
			
			// byte[] data = pad(content.getBytes());
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

	public static String Cardencrypt(String Secretkey, String json) {
		String mPos = null;
		try {
			String key = sha1(Secretkey).toLowerCase();
			mPos = encrypt(json, key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mPos;
	}

	public String encryptPaymentDataAccess(UnifiedCardRequest card) {
		String jsonString = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			String expMonyear = card.getExpiry();
			log.info(expMonyear);
			card.setExpiry("MONYR");
			card.setSecretKey(merchantSecret);
			String json = mapper.writeValueAsString(card);
			log.info("Result JSON String = " + json);
			//json = StringEscapeUtils.escapeJson(json);
			//log.info("JSON = " + json);
			json = json.replace("MONYR", expMonyear);
			log.info("JSON = " + json);
			String key = sha1(merchantSecret).toLowerCase();
			log.info(key);
			jsonString = encrypt(json, key);
			log.info("JSON Serial = " + jsonString);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return jsonString;
	}

	public WayaTransactionQuery transactionQuery(String tranId) {
		HttpHeaders headers = new HttpHeaders();
		String baseUrl = merchantUrl + "/Status/" + tranId;
		UriComponentsBuilder builderURL = UriComponentsBuilder.fromHttpUrl(baseUrl);
		log.info("BASE URL= " + builderURL.toUriString());
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<WayaTransactionQuery> resp = restTemplate.exchange(builderURL.toUriString(), HttpMethod.GET,
				entity, WayaTransactionQuery.class);
		log.info("Return Message: " + resp.getBody());
		return resp.getBody();
	}

	public WayaTransactionQuery postPayAttitude(WayaPaymentCallback pay) {
		try {
			UnifiedPaymentCallback callReq = new UnifiedPaymentCallback(pay.getTranId(), merchantId,
					pay.getCardEncrypt());
			log.info("Unified Payment Request: {}", callReq.toString());

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
	
	public static void setKey(String myKey) 
    {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); 
            secretKey = new SecretKeySpec(key, "AES");
        } 
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } 
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
 
    public static String getDataEncrypt(String strToEncrypt, String secret) 
    {
        try
        {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } 
        catch (Exception e) 
        {
            System.out.println("Error while encrypting: " + e.getLocalizedMessage() + " : " + e.getMessage());
        }
        return null;
    }
 
    public static String getDataDecrypt(String strToDecrypt, String secret) 
    {
        try
        {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } 
        catch (Exception e) 
        {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }
}
