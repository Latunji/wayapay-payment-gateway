package com.wayapaychat.paymentgateway.service.impl;

import com.wayapaychat.paymentgateway.entity.PaymentProcessor;
import com.wayapaychat.paymentgateway.entity.ProcessorConfiguration;
import com.wayapaychat.paymentgateway.pojo.waya.ErrorResponse;
import com.wayapaychat.paymentgateway.pojo.waya.ProcessorConfigurationRequest;
import com.wayapaychat.paymentgateway.pojo.waya.ProcessorRequest;
import com.wayapaychat.paymentgateway.pojo.waya.SuccessResponse;
import com.wayapaychat.paymentgateway.repository.PaymentProcessorRepository;
import com.wayapaychat.paymentgateway.repository.ProcessorConfigurationRepository;
import com.wayapaychat.paymentgateway.service.PaymentProcessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.List;

@Service
@Slf4j
public class PaymentProcessorServiceImpl implements PaymentProcessorService {
    @Autowired
    PaymentProcessorRepository paymentProcessorRepository;

    @Autowired
    ProcessorConfigurationRepository processorConfigurationRepository;

    // TODO Validate that the current user is a super admin.
    @Override
    public ResponseEntity<?> createProcessor(ProcessorRequest request, String token) {
//        ResponseEntity<?> response = new ResponseEntity<>(new ErrorResponse("Unprocessed Request"),
//                HttpStatus.BAD_REQUEST);

        PaymentProcessor paymentProcessor = paymentProcessorRepository.findByName(request.getName()).orElse(null);
        if (paymentProcessor != null) {
            return new ResponseEntity<>(new ErrorResponse("Oops, This processor already exists."), HttpStatus.BAD_REQUEST);
        }

        try {
            PaymentProcessor processor = new PaymentProcessor();
            processor.setName(request.getName());
            processor.setDescription(request.getDescription());
            processor.setCode(request.getName().trim().replace(" ", "_"));
            paymentProcessorRepository.save(processor);

            return new ResponseEntity<>(new SuccessResponse("CREATED", processor), HttpStatus.CREATED);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
//        return response;
    }

    @Override
    public ResponseEntity<?> fetchAllProcessor(String token) {
        List<PaymentProcessor> processorList = paymentProcessorRepository.findAll();
        return new ResponseEntity<>(new SuccessResponse("SUCCESSFUL", processorList), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> fetchProcessorConfigurations(String token) {
        ProcessorConfiguration configurations = processorConfigurationRepository.getProcessorConfigurations();
        return new ResponseEntity<>(new SuccessResponse("SUCCESSFUL", configurations), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> setupProcessorConfigurations(ProcessorConfigurationRequest request, String token) {
        ProcessorConfiguration configurations = processorConfigurationRepository.getProcessorConfigurations();
        if (ObjectUtils.isEmpty(configurations)) {
            configurations = new ProcessorConfiguration();
        }
        String msg = "";

        PaymentProcessor cardProcessor = paymentProcessorRepository.findByName(request.getCardAcquiring()).orElse(null);
        PaymentProcessor ussdProcessor = paymentProcessorRepository.findByName(request.getUssdAcquiring()).orElse(null);
        PaymentProcessor accountProcessor = paymentProcessorRepository.findByName(request.getAccountAcquiring()).orElse(null);
        PaymentProcessor payattitudeProcessor = paymentProcessorRepository.findByName(request.getPayattitudeAcquiring()).orElse(null);

        if (cardProcessor != null) {
            configurations.setCardAcquiring(cardProcessor.getName());
            msg = msg+"Card Acquiring Set. ";
        }

        if (ussdProcessor != null) {
            configurations.setUssdAcquiring(ussdProcessor.getName());
            msg = msg+"USSD Acquiring Set. ";
        }

        if (accountProcessor != null) {
            configurations.setAccountAcquiring(accountProcessor.getName());
            msg = msg+"Account Acquiring Set. ";
        }

        if (payattitudeProcessor != null) {
            configurations.setPayattitudeAcquiring(payattitudeProcessor.getName());
            msg = msg+"PayAttitude Acquiring Set.";
        }

        processorConfigurationRepository.save(configurations);
        return new ResponseEntity<>(new SuccessResponse(msg, configurations), HttpStatus.OK);
    }
}
