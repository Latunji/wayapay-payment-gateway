package com.wayapaychat.paymentgateway.controller;


import com.wayapaychat.paymentgateway.common.utils.PageableResponseUtil;
import com.wayapaychat.paymentgateway.pojo.waya.PaginationPojo;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.waya.QueryRecurrentTransactionPojo;
import com.wayapaychat.paymentgateway.service.RecurrentTransactionService;
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
@RequestMapping("/api/v1/recurrent-transactions")
@Tag(name = "RECURRENT-TRANSACTION", description = "Payment gateway fraud rule APIs")
@Validated
@AllArgsConstructor
public class RecurrentTransactionController {
    private final RecurrentTransactionService recurrentTransactionService;

//    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Filter Search recurrent customer transactions", notes = "recurrent customer transactions",tags = {"RECURRENT-TRANSACTION"})
    public ResponseEntity<PaymentGatewayResponse> filterSearchCustomerRecurrentSubscription(QueryRecurrentTransactionPojo queryCustomerTransactionPojo) {
        return recurrentTransactionService.filterSearchRecurrentTransaction(
                queryCustomerTransactionPojo, PageableResponseUtil.createPageRequest(queryCustomerTransactionPojo.getPage(),
                        queryCustomerTransactionPojo.getSize(), queryCustomerTransactionPojo.getOrder(),
                        queryCustomerTransactionPojo.getSortBy(), true, "date_created"
                ));
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/fetch/{customerId}")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Filter Search recurrent customer transactions", notes = "recurrent customer transactions", tags = {"RECURRENT-TRANSACTION"})
    public ResponseEntity<PaymentGatewayResponse> fetchCustomerRecurrentTransaction(
            @PathVariable String customerId, PaginationPojo paginationPojo) {
        return recurrentTransactionService.fetchCustomerTransaction(
                customerId, PageableResponseUtil.createPageRequest(paginationPojo.getPage(),
                        paginationPojo.getSize(), paginationPojo.getOrder(),
                        paginationPojo.getSortBy(), true, "date_created"
                ));
    }
}
