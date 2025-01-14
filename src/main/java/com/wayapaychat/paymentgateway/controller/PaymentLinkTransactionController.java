package com.wayapaychat.paymentgateway.controller;

import com.wayapaychat.paymentgateway.common.utils.PageableResponseUtil;
import com.wayapaychat.paymentgateway.pojo.waya.PaginationPojo;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.service.impl.PaymentGatewayServiceImpl;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/transactions/payment-link")
@Tag(name = "PAYMENT-LINK-TRANSACTIONS", description = "Payment gateway transaction APIs")
@Validated
@AllArgsConstructor
public class PaymentLinkTransactionController {
    private final PaymentGatewayServiceImpl paymentGatewayService;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/{paymentLinkId}/fetch-all")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Filter Search transaction for a payment link", notes = "Get all transactions made using payment link", tags = {"PAYMENT-LINK-TRANSACTIONS"})
    public ResponseEntity<PaymentGatewayResponse> fetchPaymentLinkTransactions(
            @PathVariable String paymentLinkId, @RequestParam(required = false) String merchantId, @RequestHeader("Authorization") String token, PaginationPojo paginationPojo) {
        return paymentGatewayService.fetchPaymentLinkTransactions(
                merchantId, paymentLinkId, token, PageableResponseUtil.createPageRequest(paginationPojo.getPage(),
                        paginationPojo.getSize(), paginationPojo.getOrder(),
                        paginationPojo.getSortBy(), true, "tran_date"
                ));
    }

}
