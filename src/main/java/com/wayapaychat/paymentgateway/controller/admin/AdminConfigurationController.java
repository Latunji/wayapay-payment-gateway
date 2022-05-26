package com.wayapaychat.paymentgateway.controller.admin;


import com.wayapaychat.paymentgateway.pojo.SettlementPricingConfigurationPojo;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

//@CrossOrigin
//@RestController
//@RequestMapping("/api/v1/configuration")
//@Tag(name = "PAYMENT-GATEWAY-CONFIGURATION", description = "Api for admin to manage payment gateway configuration")
//@Validated
//@Slf4j
public class AdminConfigurationController {
    private SettlementPricingConfigurationService settlementPricingConfigurationService;


    @ApiOperation(value = "Get settlement pricing for payment channel", notes = "Get All settlement configuration", tags = {"SETTLEMENT-PRICING"})
    @GetMapping("/settlement-pricing")
    public ResponseEntity<?> getSettlementPricingConfiguration() {
        return settlementPricingConfigurationService.getAll();
    }

    @ApiOperation(value = "Configure settlement pricing for payment channel", notes = "Endpoint to create settlement pricing configuration", tags = {"SETTLEMENT-PRICING"})
    @PostMapping("/settlement-pricing")
    public ResponseEntity<?> createSettlementPricingConfiguration(@Valid @RequestBody SettlementPricingConfigurationPojo request) {
        return settlementPricingConfigurationService.createSettlementPricingConfiguration(request);
    }
}
