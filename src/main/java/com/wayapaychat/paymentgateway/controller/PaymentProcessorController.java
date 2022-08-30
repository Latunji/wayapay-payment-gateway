package com.wayapaychat.paymentgateway.controller;

import com.wayapaychat.paymentgateway.pojo.waya.ProcessorConfigurationRequest;
import com.wayapaychat.paymentgateway.service.PaymentProcessorService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/processors")
@Tag(name = "PROCESSOR SETTINGS", description = "APIs to manage payment processors.")
@Validated
@Slf4j
public class PaymentProcessorController {
    @Autowired
    PaymentProcessorService paymentProcessorService;

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, value = "")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Create new processor", notes = "Create a new processor (ADMIN)")
    public ResponseEntity<?> configureNewProcessor(@RequestBody ProcessorConfigurationRequest config, @RequestHeader("Authorization") String token) {
        return paymentProcessorService.configureNewProcessor(config, token);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Fetch all processors", notes = "Returns a list of all processors (ADMIN)")
    public ResponseEntity<?> fetchAllProcessor(@RequestHeader("Authorization") String token) {
        return paymentProcessorService.fetchAllProcessor(token);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/{nameOrProcessorCode}")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Fetch all transaction configurations", notes = "Returns a list of configuration setup for each transaction type (ADMIN)")
    public ResponseEntity<?> fetchProcessorConfigurations(@RequestHeader("Authorization") String token, @PathVariable("nameOrProcessorCode") final String nameOrProcessorCode) {
        return paymentProcessorService.fetchProcessorByNameOrCode(token, nameOrProcessorCode);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, value = "/{processorCode}")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataType = "string", dataTypeClass = String.class)})
    @ApiOperation(value = "Set transaction configurations", notes = "Configuration setup for each transaction type (ADMIN)")
    public ResponseEntity<?> updateProcessorConfigurations(@RequestBody ProcessorConfigurationRequest request, @RequestHeader("Authorization") String token, @PathVariable("processorCode") final String processorCode) {
        return paymentProcessorService.updateProcessorConfigurations(request, processorCode, token);
    }
}
