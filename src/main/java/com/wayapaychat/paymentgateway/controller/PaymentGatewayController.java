package com.wayapaychat.paymentgateway.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wayapaychat.paymentgateway.pojo.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaCallbackRequest;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaCardPayment;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaPaymentCallback;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaPaymentRequest;
import com.wayapaychat.paymentgateway.service.PaymentGatewayService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "PAYMENT-GATEWAY", description = "Payment Gateway User Service API")
@Validated
@Slf4j
public class PaymentGatewayController {

	@Autowired
	PaymentGatewayService paymentGatewayService;

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
	
	@ApiOperation(value = "Waya-Request Transaction", notes = "This endpoint create client user", tags = { "PAYMENT-GATEWAY" })
	//@ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@PostMapping("/request/transaction")
	public ResponseEntity<?> PostCardRequest(HttpServletRequest request,@RequestHeader("authorization") String token,
			@Valid @RequestBody WayaPaymentRequest account) {
		PaymentGatewayResponse resp = paymentGatewayService.CardAcquireRequest(request, account,token);
		if (!resp.getStatus()) {
			return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(resp, HttpStatus.OK);

	}
	
	@ApiOperation(value = "Waya-Payment Card Processing", notes = "This endpoint create client user", tags = { "PAYMENT-GATEWAY" })
	@ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@PostMapping("/transaction/payment")
	public ResponseEntity<?> PostCardPayment(HttpServletRequest request,
			@Valid @RequestBody WayaCardPayment card) {
		PaymentGatewayResponse resp = paymentGatewayService.CardAcquirePayment(request, card);
		if (!resp.getStatus()) {
			return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(resp, HttpStatus.OK);

	}
	
	@ApiOperation(value = "Request Callback URL", notes = "This endpoint create client user", tags = { "PAYMENT-GATEWAY" })
	@ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@PostMapping("/transaction/processing")
	public ResponseEntity<?> PostCallbackCard(HttpServletRequest request, HttpServletResponse response,
			@Valid @RequestBody WayaPaymentCallback pay) {
		PaymentGatewayResponse resp = paymentGatewayService.CardAcquireCallback(request, response, pay);
		if (!resp.getStatus()) {
			return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(resp, HttpStatus.OK);

	}
	
	@ApiOperation(value = "Request Callback URL", notes = "This endpoint create client user", tags = { "PAYMENT-GATEWAY" })
	@ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@PostMapping("/transaction/processing/bank")
	public ResponseEntity<?> PostCallbackPayAttitude(HttpServletRequest request,
			@Valid @RequestBody WayaPaymentCallback pay) {
		PaymentGatewayResponse resp = paymentGatewayService.PayAttitudeCallback(request, pay);
		if (!resp.getStatus()) {
			return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(resp, HttpStatus.OK);

	}
	
	@PostMapping(
            value = "/wayaCallBack",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
	@ApiOperation(value = "Waya Callback URL", notes = "This endpoint create client user", tags = { "PAYMENT-GATEWAY" })
	public ResponseEntity<?> CallBack(@RequestBody WayaCallbackRequest requests) {
		log.info(requests.toString());
		return null;
	}
	
	@ApiOperation(value = "Get Transaction Status", notes = "This endpoint transaction status", tags = { "PAYMENT-GATEWAY" })
	@ApiImplicitParams({@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@GetMapping("/transaction/query/{tranId}")
	public ResponseEntity<?> getTransactionStatus(HttpServletRequest request, 
			@PathVariable("tranId") final String tranId) {
		return paymentGatewayService.GetTransactionStatus(request, tranId);
	}

}
