package com.wayapaychat.paymentgateway.service;

import com.wayapaychat.paymentgateway.pojo.waya.ProcessorConfigurationRequest;
import com.wayapaychat.paymentgateway.pojo.waya.ProcessorRequest;
import org.springframework.http.ResponseEntity;

public interface PaymentProcessorService {
    ResponseEntity<?> createProcessor(ProcessorRequest request, String token);

    ResponseEntity<?> fetchAllProcessor(String token);

    ResponseEntity<?> fetchProcessorConfigurations(String token);

    ResponseEntity<?> setupProcessorConfigurations(ProcessorConfigurationRequest request, String token);
}
