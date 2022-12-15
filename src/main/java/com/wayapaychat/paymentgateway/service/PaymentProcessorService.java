package com.wayapaychat.paymentgateway.service;

import com.wayapaychat.paymentgateway.pojo.waya.ProcessorConfigurationRequest;
import org.springframework.http.ResponseEntity;

public interface PaymentProcessorService {
    ResponseEntity<?> configureNewProcessor(ProcessorConfigurationRequest request, String token);

    ResponseEntity<?> fetchAllProcessor(String token);

    ResponseEntity<?> fetchProcessorByNameOrCode(String token, String nameOrCode);

    ResponseEntity<?> updateProcessorConfigurations(ProcessorConfigurationRequest request, String code, String token);
}
