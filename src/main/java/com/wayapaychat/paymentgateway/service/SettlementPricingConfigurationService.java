package com.wayapaychat.paymentgateway.service;

import com.wayapaychat.paymentgateway.pojo.SettlementPricingConfigurationPojo;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import org.springframework.http.ResponseEntity;

public interface SettlementPricingConfigurationService {
    ResponseEntity<PaymentGatewayResponse> getAll();

    ResponseEntity<?> createSettlementPricingConfiguration(SettlementPricingConfigurationPojo settlementPricingConfigurationPojo);
}
