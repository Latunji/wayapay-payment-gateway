package com.wayapaychat.paymentgateway.controller;


import com.wayapaychat.paymentgateway.common.utils.PageableResponseUtil;
import com.wayapaychat.paymentgateway.pojo.waya.PaginationPojo;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.waya.QueryCustomerTransactionPojo;
import com.wayapaychat.paymentgateway.pojo.waya.QueryRecurrentTransactionPojo;
import com.wayapaychat.paymentgateway.service.PaymentGatewayService;
import com.wayapaychat.paymentgateway.service.RecurrentTransactionService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "TRANSACTIONS", description = "Payment gateway transaction APIs")
@Validated
@AllArgsConstructor
public class TransactionController {
    private final RecurrentTransactionService recurrentTransactionService;
    @Autowired
    PaymentGatewayService paymentGatewayService;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/{customerId}")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Filter search customers", notes = "Search customers", tags = {"TRANSACTIONS"})
    public ResponseEntity<PaymentGatewayResponse> filterSearchAllCustomerTransactions(
            QueryCustomerTransactionPojo queryCustomerTransactionPojo,
            @PathVariable("customerId") final String customerId) {
        queryCustomerTransactionPojo.setCustomerId(customerId);
        return paymentGatewayService.filterSearchCustomerTransactions(queryCustomerTransactionPojo, PageableResponseUtil.createPageRequest(queryCustomerTransactionPojo.getPage(),
                queryCustomerTransactionPojo.getSize(), queryCustomerTransactionPojo.getOrder(),
                queryCustomerTransactionPojo.getSortBy(), true, "tran_date"
        ));
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/report/year-month-stats")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Get transaction statistics by date-range", notes = "Transaction Year-Month statistics", tags = {"TRANSACTIONS"})
    public ResponseEntity<PaymentGatewayResponse> getYearMonthTransactionStats(
            @RequestParam(value = "merchantId", required = false) final String merchantId,
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "year", required = false) final Long year,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ) {
        return paymentGatewayService.getYearMonthTransactionStats(merchantId, year, startDate, endDate, token);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/report/overview")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Get transaction report overview statistics for admin or merchant", notes = "Transaction report overview Stats for admin or merchant", tags = {"TRANSACTIONS"})
    public ResponseEntity<PaymentGatewayResponse> getDashboardOverviewStats(@RequestParam(value = "merchantId", required = false) final String merchantId, @RequestHeader("Authorization") String token) {
        return paymentGatewayService.getTransactionOverviewStats(merchantId, token);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/report/revenue-stats")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Get transaction net and gross revenue report for admin or merchant", notes = "Transaction net and gross revenue report", tags = {"TRANSACTIONS"})
    public ResponseEntity<PaymentGatewayResponse> getTransactionGrossAndNetRevenue(
            @RequestParam(value = "merchantId", required = false) final String merchantId, @RequestHeader("Authorization") String token) {
        return paymentGatewayService.getTransactionGrossAndNetRevenue(merchantId, token);
    }
}
