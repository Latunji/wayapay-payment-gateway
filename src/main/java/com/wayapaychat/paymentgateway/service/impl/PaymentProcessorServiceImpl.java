package com.wayapaychat.paymentgateway.service.impl;

import com.wayapaychat.paymentgateway.entity.ProcessorConfiguration;
import com.wayapaychat.paymentgateway.pojo.waya.ErrorResponse;
import com.wayapaychat.paymentgateway.pojo.waya.ProcessorConfigurationRequest;
import com.wayapaychat.paymentgateway.pojo.waya.SuccessResponse;
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
import java.util.*;

@Service
@Slf4j
public class PaymentProcessorServiceImpl implements PaymentProcessorService {
    @Autowired
    ProcessorConfigurationRepository processorConfigurationRepository;

    // TODO Validate that the current user is a super admin. (Use @PreAuthorise on controller to achieve these)
    @Override
    public ResponseEntity<?> configureNewProcessor(ProcessorConfigurationRequest request, String token) {
//        ResponseEntity<?> response = new ResponseEntity<>(new ErrorResponse("Unprocessed Request"),
//                HttpStatus.BAD_REQUEST);

        ProcessorConfiguration processorConfiguration = processorConfigurationRepository.findByName(request.getName()).orElse(null);
        if (processorConfiguration != null) {
            return new ResponseEntity<>(new ErrorResponse("Oops, This processor already exists."), HttpStatus.BAD_REQUEST);
        }

        try {
            ProcessorConfiguration processor = new ProcessorConfiguration();
            processor.setName(request.getName());
            processor.setDescription(request.getDescription());
            processor.setCode(request.getName().trim().replace(" ", "_"));
//            processor.setTestBaseUrl(request.getTestBaseUrl());
//            processor.setLiveBaseUrl(request.getLiveBaseUrl());
            processor.setPayattitudeAcquiring(request.getPayattitudeAcquiring());
            processor.setCardAcquiring(request.getCardAcquiring());
            processor.setAccountAcquiring(request.getAccountAcquiring());
            processor.setUssdAcquiring(request.getUssdAcquiring());
            processorConfigurationRepository.save(processor);

            return new ResponseEntity<>(new SuccessResponse("CREATED", processor), HttpStatus.CREATED);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
//        return response;
    }

    @Override
    public ResponseEntity<?> fetchAllProcessor(String token) {
        List<ProcessorConfiguration> processorList = processorConfigurationRepository.findAll();
        return new ResponseEntity<>(new SuccessResponse("SUCCESSFUL", processorList), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> fetchProcessorByNameOrCode(String token, String nameOrCode) {
        Optional<ProcessorConfiguration> configuration = processorConfigurationRepository.findByNameOrCode(nameOrCode);
        if (configuration.isPresent())
            return new ResponseEntity<>(new SuccessResponse("SUCCESSFUL", configuration.get()), HttpStatus.OK);
        return new ResponseEntity<>(new SuccessResponse("Data NOT FOUND", configuration), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> updateProcessorConfigurations(ProcessorConfigurationRequest updateRequest, String code, String token) {
        Optional<ProcessorConfiguration> configuration = processorConfigurationRepository.findByNameOrCode(code);
        if (configuration.isPresent()) {
            ProcessorConfiguration config = configuration.get();
            config.setName(updateRequest.getName());
            config.setDescription(updateRequest.getDescription());
//            config.setLiveBaseUrl(updateRequest.getLiveBaseUrl());
//            config.setTestBaseUrl(updateRequest.getTestBaseUrl());
            config.setAccountAcquiring(updateRequest.getAccountAcquiring());
            config.setCardAcquiring(updateRequest.getCardAcquiring());
            config.setUssdAcquiring(updateRequest.getUssdAcquiring());
            config.setPayattitudeAcquiring(updateRequest.getPayattitudeAcquiring());

            processorConfigurationRepository.save(config);

            return new ResponseEntity<>(new SuccessResponse("SUCCESSFUL", config), HttpStatus.OK);
        }
        return new ResponseEntity<>(new SuccessResponse("DATA NOT FOUND", null), HttpStatus.OK);
    }
}
