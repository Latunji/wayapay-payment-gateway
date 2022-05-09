package com.wayapaychat.paymentgateway.kafkamessagebroker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamDataSMS {
    private List<RecipientsSMS> recipients;
    private String message;
}
