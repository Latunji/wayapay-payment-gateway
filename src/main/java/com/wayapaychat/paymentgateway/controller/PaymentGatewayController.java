package com.wayapaychat.paymentgateway.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.enumm.TransactionStatus;
import com.wayapaychat.paymentgateway.pojo.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaCallbackRequest;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaCardPayment;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaDecypt;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaEncypt;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaPaymentCallback;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaPaymentRequest;
import com.wayapaychat.paymentgateway.pojo.ussd.USSDWalletPayment;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDPayment;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDRequest;
import com.wayapaychat.paymentgateway.pojo.waya.WayaAuthenicationRequest;
import com.wayapaychat.paymentgateway.pojo.waya.WayaPaymentStatus;
import com.wayapaychat.paymentgateway.pojo.waya.WayaQRRequest;
import com.wayapaychat.paymentgateway.pojo.waya.WayaWalletPayment;
import com.wayapaychat.paymentgateway.pojo.waya.WayaWalletRequest;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import com.wayapaychat.paymentgateway.service.PaymentGatewayService;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

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

	/*
	 * @ApiOperation(value = "Wema Transaction Query", notes =
	 * "This endpoint create client user", tags = { "PAYMENT-GATEWAY" })
	 * 
	 * @ApiImplicitParams({
	 * 
	 * @ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value
	 * = "token", paramType = "header", required = true) })
	 * 
	 * @PostMapping("/wema/tranQuery") public ResponseEntity<?>
	 * createPostAccount(HttpServletRequest request,
	 * 
	 * @Valid @RequestBody WemaTxnQueryRequest account) { PaymentGatewayResponse
	 * resp = paymentGatewayService.wemaTransactionQuery(request, account); if
	 * (!resp.getStatus()) { return new ResponseEntity<>(resp,
	 * HttpStatus.BAD_REQUEST); } return new ResponseEntity<>(resp, HttpStatus.OK);
	 * 
	 * }
	 * 
	 * @ApiOperation(value = "Wema Prefix", notes =
	 * "This endpoint create client user", tags = { "PAYMENT-GATEWAY" })
	 * 
	 * @GetMapping("/wema/prefix") public ResponseEntity<?>
	 * getAllPrefix(HttpServletRequest request) { PaymentGatewayResponse resp =
	 * paymentGatewayService.wemaAllPrefix(request); if (!resp.getStatus()) { return
	 * new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST); }
	 * log.info("Response received --- {}", "wemaPrefix", resp); return new
	 * ResponseEntity<>(resp, HttpStatus.OK);
	 * 
	 * }
	 */
	@ApiOperation(value = "QR-Code Waya-Request Transaction", notes = "This endpoint create client user", tags = {
			"PAYMENT-GATEWAY" })
	// @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
	// String.class, value = "token", paramType = "header", required = true) })
	@PostMapping("/generate/qr-code")
	public ResponseEntity<?> PostPaymentQRCode(HttpServletRequest request, @Valid @RequestBody WayaQRRequest account) {
		return paymentGatewayService.WalletPaymentQR(request, account);
	}

	@ApiOperation(value = "USSD Waya-Request Transaction", notes = "This endpoint create client user", tags = {
			"PAYMENT-GATEWAY" })
// @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
// String.class, value = "token", paramType = "header", required = true) })
	@PostMapping("/request/ussd")
	public ResponseEntity<?> PostPaymentUSSD(HttpServletRequest request, @Valid @RequestBody WayaUSSDRequest account) {
		return paymentGatewayService.USSDPaymentRequest(request, account);
	}

	@ApiOperation(value = "USSD Waya-Payment Transaction", notes = "This endpoint create client user", tags = {
			"PAYMENT-GATEWAY" })
//@ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
//String.class, value = "token", paramType = "header", required = true) })
	@PutMapping("/payment/ussd/{refNo}")
	public ResponseEntity<?> PostUSSD(HttpServletRequest request, @Valid @RequestBody WayaUSSDPayment account,
			@PathVariable("refNo") final String refNo) {
		return paymentGatewayService.USSDPayment(request, account, refNo);
	}

	@ApiOperation(value = "USSD Wallet Transaction", notes = "This endpoint create client user", tags = {
			"PAYMENT-GATEWAY" })
//@ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
//String.class, value = "token", paramType = "header", required = true) })
	@PostMapping("/payment/ussd/wallet")
	public ResponseEntity<?> PostWalletUSSD(HttpServletRequest request, @Valid @RequestBody USSDWalletPayment account) {
		return paymentGatewayService.USSDWalletPayment(request, account);
	}

	@ApiOperation(value = "Wallet Waya-Request Transaction", notes = "This endpoint create client user", tags = {
			"PAYMENT-GATEWAY" })
// @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
// String.class, value = "token", paramType = "header", required = true) })
	@PostMapping("/request/wallet")
	public ResponseEntity<?> PostPaymentWallet(HttpServletRequest request,
			@Valid @RequestBody WayaWalletRequest account) {
		return paymentGatewayService.PostWalletPayment(request, account);
	}

	@ApiOperation(value = "Wallet Authentication", notes = "This endpoint create client user", tags = {
			"PAYMENT-GATEWAY" })
	// @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
	// String.class, value = "token", paramType = "header", required = true) })
	@PostMapping("/authentication/wallet")
	public ResponseEntity<?> PostPaymentAuthentication(HttpServletRequest request,
			@Valid @RequestBody WayaAuthenicationRequest account) {
		return paymentGatewayService.WalletPaymentAuthentication(request, account);
	}

	@ApiOperation(value = "Wallet Waya-Payment Processing", notes = "This endpoint create client user", tags = {
			"PAYMENT-GATEWAY" })
	// @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
	// String.class, value = "token", paramType = "header", required = true) })
	@PostMapping("/wallet/payment")
	public ResponseEntity<?> PostWalletPayment(HttpServletRequest request,
			@Valid @RequestBody WayaWalletPayment payment, @RequestHeader("Authorization") String token) {
		return paymentGatewayService.ConsumeWalletPayment(request, payment, token);

	}

	@ApiOperation(value = "Get Transaction Status", notes = "This endpoint transaction status", tags = {
			"PAYMENT-GATEWAY" })
	// @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
	// String.class, value = "token", paramType = "header", required = true) })
	@GetMapping("/wallet/query/{tranId}")
	public ResponseEntity<?> getWalletTransaction(HttpServletRequest request,
			@PathVariable("tranId") final String tranId) {
		return paymentGatewayService.GetTransactionStatus(request, tranId);
	}

	@ApiOperation(value = "Waya-Request Transaction", notes = "This endpoint create client user", tags = {
			"PAYMENT-GATEWAY" })
	// @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
	// String.class, value = "token", paramType = "header", required = true) })
	@PostMapping("/request/transaction")
	public ResponseEntity<?> PostCardRequest(HttpServletRequest request,
			@Valid @RequestBody WayaPaymentRequest account) {
		PaymentGatewayResponse resp = paymentGatewayService.CardAcquireRequest(request, account);
		if (!resp.getStatus()) {
			return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(resp, HttpStatus.OK);

	}

	@ApiOperation(value = "Waya-Payment Card Processing", notes = "This endpoint create client user", tags = {
			"PAYMENT-GATEWAY" })
	// @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
	// String.class, value = "token", paramType = "header", required = true) })
	@PostMapping("/transaction/payment")
	public ResponseEntity<?> PostCardPayment(HttpServletRequest request, @Valid @RequestBody WayaCardPayment card) {
		PaymentGatewayResponse resp = paymentGatewayService.CardAcquirePayment(request, card);
		if (!resp.getStatus()) {
			return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(resp, HttpStatus.OK);

	}

	@ApiOperation(value = "Request Callback URL", notes = "This endpoint create client user", tags = {
			"PAYMENT-GATEWAY" })
	// @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
	// String.class, value = "token", paramType = "header", required = true) })
	@PostMapping("/transaction/processing")
	public ResponseEntity<?> PostCallbackCard(HttpServletRequest request, HttpServletResponse response,
			@Valid @RequestBody WayaPaymentCallback pay) {
		PaymentGatewayResponse resp = paymentGatewayService.CardAcquireCallback(request, response, pay);
		if (!resp.getStatus()) {
			return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(resp, HttpStatus.OK);

	}

	@ApiOperation(value = "Request Callback URL", notes = "This endpoint create client user", tags = {
			"PAYMENT-GATEWAY" })
	// @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
	// String.class, value = "token", paramType = "header", required = true) })
	@PostMapping("/transaction/processing/bank")
	public ResponseEntity<?> PostCallbackPayAttitude(HttpServletRequest request,
			@Valid @RequestBody WayaPaymentCallback pay) {
		PaymentGatewayResponse resp = paymentGatewayService.PayAttitudeCallback(request, pay);
		if (!resp.getStatus()) {
			return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(resp, HttpStatus.OK);

	}

	@PostMapping(value = "/wayaCallBack", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	@ApiOperation(value = "Waya Callback URL", notes = "This endpoint create client user", tags = { "PAYMENT-GATEWAY" })
	public String CallBack(WayaCallbackRequest requests) {
		log.info(requests.toString());
		PaymentGateway payment = paymentGatewayRepo.findByTranId(requests.getTrxId()).orElse(null);
		if (payment == null) {
			return "UNKNOWN PAYMENT TRANSACTION STATUS";
		}

		if (requests.isApproved()) {
			payment.setStatus(TransactionStatus.SUCCESSFUL);
			payment.setSuccessfailure(true);
			payment.setTranId(requests.getTrxId());
		} else {
			payment.setStatus(TransactionStatus.FAILED);
			payment.setSuccessfailure(false);
			payment.setTranId(requests.getTrxId());
		}
		return "WAYA PAYMENT TRANSACTION " + requests.getStatus().toUpperCase();
	}

	@ApiOperation(value = "Get Transaction Status", notes = "This endpoint transaction status", tags = {
			"PAYMENT-GATEWAY" })
	// @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
	// String.class, value = "token", paramType = "header", required = true) })
	@GetMapping("/transaction/query/{tranId}")
	public ResponseEntity<?> getTransactionStatus(HttpServletRequest request,
			@PathVariable("tranId") final String tranId) {
		return paymentGatewayService.GetTransactionStatus(request, tranId);
	}

	@ApiOperation(value = "Get Reference", notes = "This endpoint transaction status", tags = { "PAYMENT-GATEWAY" })
// @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
// String.class, value = "token", paramType = "header", required = true) })
	@GetMapping("/reference/query/{refNo}")
	public ResponseEntity<?> getReference(HttpServletRequest request, @PathVariable("refNo") final String refNo) {
		return paymentGatewayService.GetReferenceStatus(request, refNo);
	}

	@ApiOperation(value = "Update Transaction status", notes = "This endpoint transaction status", tags = {
			"PAYMENT-GATEWAY" })
	// @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
	// String.class, value = "token", paramType = "header", required = true) })
	@PutMapping("/transaction/status/{refNo}")
	public ResponseEntity<?> updateRefStatus(HttpServletRequest request, @PathVariable("refNo") final String refNo,
			@Valid @RequestBody WayaPaymentStatus pay) {
		return paymentGatewayService.postRefStatus(request, refNo, pay);
	}

	@ApiOperation(value = "Card Encryption", notes = "This endpoint create client user", tags = { "PAYMENT-GATEWAY" })
	// @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
	// String.class, value = "token", paramType = "header", required = true) })
	@PostMapping("/card/encryption")
	public ResponseEntity<?> PostCardEncrypt(HttpServletRequest request, @Valid @RequestBody WayaEncypt pay) {
		PaymentGatewayResponse resp = paymentGatewayService.encrypt(request, pay);
		if (!resp.getStatus()) {
			return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(resp, HttpStatus.OK);

	}

	@ApiOperation(value = "Card Decryption", notes = "This endpoint create client user", tags = { "PAYMENT-GATEWAY" })
	// @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
	// String.class, value = "token", paramType = "header", required = true) })
	@PostMapping("/card/decryption")
	public ResponseEntity<?> PostCardDecrypt(HttpServletRequest request, @Valid @RequestBody WayaDecypt pay) {
		PaymentGatewayResponse resp = paymentGatewayService.decrypt(request, pay);
		if (!resp.getStatus()) {
			return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(resp, HttpStatus.OK);

	}

	@ApiOperation(value = "Get Transaction Status", notes = "This endpoint transaction status", tags = {
			"PAYMENT-GATEWAY" })
// @ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
// String.class, value = "token", paramType = "header", required = true) })
	@GetMapping("/report/query")
	public ResponseEntity<?> getQueryTransactionStatus(HttpServletRequest request) {
		return paymentGatewayService.QueryTranStatus(request);
	}

	@ApiOperation(value = "Get Transaction Status", notes = "This endpoint transaction status", tags = {
			"PAYMENT-GATEWAY" })
//@ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass =
//String.class, value = "token", paramType = "header", required = true) })
	@GetMapping("/report/query/{merchantId}")
	public ResponseEntity<?> getQueryTransactionStatus(HttpServletRequest request,
			@PathVariable("merchantId") final String merchantId) {
		return paymentGatewayService.QueryMerchantTranStatus(request, merchantId);
	}

	@ApiOperation(value = "Get Transaction Status", notes = "This endpoint transaction status", tags = {
			"PAYMENT-GATEWAY" })
	@GetMapping("/revenue/query/{merchantId}")
	public ResponseEntity<?> getRevenueQueryTransactionStatus(HttpServletRequest request,
			@PathVariable("merchantId") final String merchantId) {
		return paymentGatewayService.QueryMerchantRevenue(request, merchantId);
	}

}
