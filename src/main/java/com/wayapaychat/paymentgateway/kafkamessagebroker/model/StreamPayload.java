package com.wayapaychat.paymentgateway.kafkamessagebroker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamPayload<T> {
    private T data;
    private String initiator;
    private String eventType;
    private String token;
    @JsonIgnore
    private String key;
}
