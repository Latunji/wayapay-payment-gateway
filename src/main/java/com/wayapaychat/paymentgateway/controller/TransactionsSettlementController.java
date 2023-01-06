package com.wayapaychat.paymentgateway.controller;


import com.wayapaychat.paymentgateway.common.utils.PageableResponseUtil;
import com.wayapaychat.paymentgateway.pojo.SettlementStatusUpdateDto;
import com.wayapaychat.paymentgateway.pojo.waya.PaginationPojo;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentListResponse;
import com.wayapaychat.paymentgateway.pojo.waya.SettlementQueryPojo;
import com.wayapaychat.paymentgateway.service.TransactionSettlementService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Path;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/transactions/settlements")
@Tag(name = "TRANSACTIONS-SETTLEMENT", description = "Transaction settlement service controller")
@Validated
@AllArgsConstructor
public class TransactionsSettlementController {
    private final TransactionSettlementService transactionSettlementService;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/fetch-all")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Get merchant settlements history and others", notes = "View the merchant settlement history", tags = {"TRANSACTIONS-SETTLEMENT"})
    public ResponseEntity<PaymentGatewayResponse> getSuccessfulTransactionSettlement(
            SettlementQueryPojo settlementQueryPojo, PaginationPojo paginationPojo,
            @RequestParam(value = "merchantId", required = false) final String merchantId) {
        return transactionSettlementService.getAllSettledSuccessfulTransactions(settlementQueryPojo,merchantId, PageableResponseUtil.createPageRequest(paginationPojo.getPage(),
                paginationPojo.getSize(), paginationPojo.getOrder(),
                paginationPojo.getSortBy(), true, "settlementDate"
        ));
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/query-all/pending")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Get all transactions pending settlement", notes = "This endpoint get all merchant transactions", tags = {"PAYMENT-GATEWAY"})
    public PaymentGatewayResponse getAllTransactionsPendingSettlement() {
        return transactionSettlementService.fetchAllTransactionsPendingSettlement();
    }

    @ApiOperation(value = "Get all merchant transactions pending settlement", notes = "This endpoint get all merchant transactions", tags = {"PAYMENT-GATEWAY"})
    @GetMapping("/query-all/pending/{merchantId}")
    public PaymentListResponse getAllMerchantTransactionsPendingSettlement(@PathVariable("merchantId") String merchantId) {
        return transactionSettlementService.fetchAllMerchantTransactionsPendingSettlement(merchantId);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, value = "/update-status")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Updates all transactions status", notes = "This endpoint update merchant transactions status", tags = {"PAYMENT-GATEWAY"})
    public PaymentGatewayResponse updateMerchantTransactions(@RequestBody SettlementStatusUpdateDto merchantId) {
        return transactionSettlementService.updateMerchantSettlement(merchantId);
    }


//    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/statistics/{merchantId}")
//    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
//    @ApiOperation(value = "Updates all transactions status", notes = "This endpoint update merchant transactions status", tags = {"PAYMENT-GATEWAY"})
//    public PaymentGatewayResponse getSettlementStats(@PathVariable("merchantId") String merchantId) {
//        return transactionSettlementService.updateMerchantSettlement(merchantId);
//    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/{settlementReferenceId}")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Get merchant settlement by reference id", notes = "View a merchant settlement by reference id", tags = {"TRANSACTIONS-SETTLEMENT"})
    public ResponseEntity<PaymentGatewayResponse> getTransactionSettlementByReferenceId(
            @PathVariable(value = "settlementReferenceId") final String settlementReferenceId) {
        return transactionSettlementService.getSettlementByReferenceId(settlementReferenceId);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/cumulative-settlement/fetch-all")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Get merchant settlements history and others", notes = "View the merchant settlement history", tags = {"TRANSACTIONS-SETTLEMENT"})
    public ResponseEntity<PaymentGatewayResponse> getCumulativeTransactionSettlement(
            SettlementQueryPojo settlementQueryPojo, PaginationPojo paginationPojo,
            @RequestParam(value = "merchantId", required = false) final String merchantId) {
        return transactionSettlementService.getCumulativeTransactionSettlement(settlementQueryPojo,merchantId, PageableResponseUtil.createPageRequest(paginationPojo.getPage(),
                paginationPojo.getSize(), paginationPojo.getOrder(),
                paginationPojo.getSortBy(), true, "date_settled"
        ));
    }

//    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/report/stats")
//    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
//    @ApiOperation(value = "Fetch settlement stats for admin or a merchant", notes = "Transaction settlement report for admin or merchant", tags = {"TRANSACTIONS-SETTLEMENT"})
//    public ResponseEntity<PaymentGatewayResponse> getSettlementStats(
//            @RequestParam(value = "merchantId", required = false) final String merchantId) {
//        return transactionSettlementService.getSettlementStats(merchantId);
//    }
}
