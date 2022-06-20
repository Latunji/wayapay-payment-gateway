package com.wayapaychat.paymentgateway.kafkamessagebroker.model;


import com.wayapaychat.paymentgateway.enumm.EventCategory;
import com.wayapaychat.paymentgateway.enumm.EventType;
import com.wayapaychat.paymentgateway.enumm.ProductType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailStreamData {
    private String message;
    private String initiator;
    private EventType eventType;
    private EventCategory eventCategory;
    private String narration;
    private ProductType productType;
}