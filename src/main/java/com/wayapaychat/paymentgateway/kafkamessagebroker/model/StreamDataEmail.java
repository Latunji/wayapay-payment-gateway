package com.wayapaychat.paymentgateway.kafkamessagebroker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamDataEmail {
    private List<RecipientsEmail> names;
    private String message;
}
