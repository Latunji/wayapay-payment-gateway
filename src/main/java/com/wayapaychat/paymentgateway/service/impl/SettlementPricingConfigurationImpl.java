package com.wayapaychat.paymentgateway.service.impl;

import com.wayapaychat.paymentgateway.common.utils.PaymentGateWayCommonUtils;
import com.wayapaychat.paymentgateway.entity.SettlementPricingConfiguration;
import com.wayapaychat.paymentgateway.enumm.Permit;
import com.wayapaychat.paymentgateway.exception.ApplicationException;
import com.wayapaychat.paymentgateway.pojo.SettlementPricingConfigurationPojo;
import com.wayapaychat.paymentgateway.pojo.waya.AuthenticatedUser;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.repository.SettlementPricingConfigurationRepository;
import com.wayapaychat.paymentgateway.service.SettlementPricingConfigurationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class SettlementPricingConfigurationImpl implements SettlementPricingConfigurationService {
    private final SettlementPricingConfigurationRepository settlementPricingConfigurationRepository;
    private final PaymentGateWayCommonUtils paymentGateWayCommonUtils;


//    @Override
    public ResponseEntity<PaymentGatewayResponse> getAll() {
        AuthenticatedUser authenticatedUser = PaymentGateWayCommonUtils.getAuthenticatedUser();
        if (authenticatedUser.getAdmin() && !authenticatedUser.getPermits().contains(Permit.UPDATE_SETTLEMENT_PRICING))
            throw new ApplicationException(403, "01", "Access denied for this user!");
        List<SettlementPricingConfiguration> settlementPricingConfigurations = settlementPricingConfigurationRepository.findAll();
        return new ResponseEntity<>(new PaymentGatewayResponse(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> createSettlementPricingConfiguration(SettlementPricingConfigurationPojo settlementPricingConfigurationPojo) {
        return null;
    }
}
