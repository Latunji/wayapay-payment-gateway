package com.wayapaychat.paymentgateway.kafkamessagebroker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargedCustomerDto {
    @NotNull
    private LocalDateTime dateCharged;
    @NotNull
    private String phoneNumber;
    @NotNull
    private String email;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    private String merchantId;
    private String customerSubscriptionId;
    private String subscriptionId;
    private String customerId;
    private String messageAfterProcessingPayment;
    private String transactionId;
}
