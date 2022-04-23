package com.wayapaychat.paymentgateway.controller;


import com.wayapaychat.paymentgateway.common.utils.PageableResponseUtil;
import com.wayapaychat.paymentgateway.pojo.waya.PaginationPojo;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
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



    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/report/stats")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Fetch the merchant settlement stats", notes = "Transaction settlement report", tags = {"TRANSACTIONS-SETTLEMENT"})
    public ResponseEntity<PaymentGatewayResponse> getMerchantSettlementStats(
            @RequestParam(value = "merchantId", required = false) final String merchantId) {
        return transactionSettlementService.getMerchantSettlementStats(merchantId);
    }
}
