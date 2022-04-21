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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "TRANSACTIONS", description = "Payment gateway transaction APIs")
@Validated
@AllArgsConstructor
public class TransactionsController {
    private final RecurrentTransactionService recurrentTransactionService;
    @Autowired
    PaymentGatewayService paymentGatewayService;

    //        @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/recurrent")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Filter Search recurrent customer transactions", notes = "recurrent customer transactions", tags = {"RECURRENT-TRANSACTION"})
    public ResponseEntity<PaymentGatewayResponse> filterSearchCustomerRecurrentSubscription(QueryRecurrentTransactionPojo queryCustomerTransactionPojo) {
        return recurrentTransactionService.filterSearchRecurrentTransaction(
                queryCustomerTransactionPojo, PageableResponseUtil.createPageRequest(queryCustomerTransactionPojo.getPage(),
                        queryCustomerTransactionPojo.getSize(), queryCustomerTransactionPojo.getOrder(),
                        queryCustomerTransactionPojo.getSortBy(), true, "date_created"
                ));
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/recurrent/fetch/{customerId}")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Filter Search recurrent customer transactions", notes = "recurrent customer transactions", tags = {"TRANSACTIONS"})
    public ResponseEntity<PaymentGatewayResponse> fetchCustomerRecurrentTransaction(
            @PathVariable String customerId, PaginationPojo paginationPojo) {
        return recurrentTransactionService.fetchCustomerTransaction(
                customerId, PageableResponseUtil.createPageRequest(paginationPojo.getPage(),
                        paginationPojo.getSize(), paginationPojo.getOrder(),
                        paginationPojo.getSortBy(), true, "date_created"
                ));
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/{customerId}")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Filter search customers", notes = "Search customers", tags = {"TRANSACTIONS"})
    public ResponseEntity<PaymentGatewayResponse> filterSearchAllCustomerSubscription(
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
    @ApiOperation(value = "Get merchant transaction year statistics stats", notes = "Transaction Year-Month statistics", tags = {"TRANSACTIONS"})
    public ResponseEntity<PaymentGatewayResponse> getMerchantYearMonthTransactionStats(
            @RequestParam(value = "merchantId", required = false) final String merchantId, @RequestParam(value = "year", required = false) final Long year) {
        return paymentGatewayService.getMerchantYearMonthTransactionStats(merchantId, year);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/report/overview")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Get merchant transaction report dashboard overview statistics", notes = "Transaction report dashboard overview Stats", tags = {"TRANSACTIONS"})
    public ResponseEntity<PaymentGatewayResponse> getMerchantDashboardOverviewStats(
            @RequestParam(value = "merchantId", required = false) final String merchantId) {
        return paymentGatewayService.getMerchantTransactionOverviewStats(merchantId);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/report/revenue-stats")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Get merchant transaction net and gross revenue report", notes = "Transaction net and gross revenue report", tags = {"TRANSACTIONS"})
    public ResponseEntity<PaymentGatewayResponse> getMerchantTransactionGrossAndNetRevenue(
            @RequestParam(value = "merchantId", required = false) final String merchantId) {
        return paymentGatewayService.getMerchantTransactionGrossAndNetRevenue(merchantId);
    }
}
