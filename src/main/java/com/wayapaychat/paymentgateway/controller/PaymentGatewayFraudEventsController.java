package com.wayapaychat.paymentgateway.controller;


import com.wayapaychat.paymentgateway.service.FraudEventService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/v1")
@Tag(name = "PAYMENT-GATEWAY-FRAUD-EVENTS", description = "Payment gateway fraud rule APIs")
@Validated
@AllArgsConstructor
public class PaymentGatewayFraudEventsController {
    private final FraudEventService paymentGatewayFraudEventService;
}
