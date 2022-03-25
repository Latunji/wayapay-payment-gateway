package com.wayapaychat.paymentgateway.service.impl;

import com.wayapaychat.paymentgateway.repository.FraudEventRepository;
import com.wayapaychat.paymentgateway.service.FraudEventService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class FraudEventImpl implements FraudEventService {
    private final FraudEventRepository fraudEventRepository;
    private final FraudRuleImpl fraudRule;


}
