package com.wayapaychat.paymentgateway.loaders;

import com.wayapaychat.paymentgateway.service.FraudEventService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FraudRuleAutoLoader implements CommandLineRunner {
    private final FraudEventService fraudEventService;
    @Override
    public void run(String... args) {
        checkIfAllFraudExists();
    }

    private void checkIfAllFraudExists() {

    }

    private void  createFraudRules(){

    }
}
