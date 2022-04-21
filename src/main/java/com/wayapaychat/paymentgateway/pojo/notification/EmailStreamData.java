package com.wayapaychat.paymentgateway.pojo.notification;


import com.wayapaychat.paymentgateway.enumm.EventCategory;
import com.wayapaychat.paymentgateway.enumm.EventType;
import com.wayapaychat.paymentgateway.enumm.PaymentChannel;
import com.wayapaychat.paymentgateway.enumm.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailStreamData {
    private String amount;
    private String initiator;
    private EventType eventType;
    private EventCategory eventCategory;
    private String narration;
    private ProductType productType;
    private String transactionId;
    private String mode;
    private String customerName;
    private String merchantName;
    private String transactionDate;
    private NotificationStreamData data;
    private String forMerchant;
    private PaymentChannel paymentChannel;
}