package com.wayapaychat.paymentgateway.kafkamessagebroker.model;

import com.wayapaychat.paymentgateway.enumm.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProducerMessageDto {
    private Object data;
    private EventType eventCategory;
}
