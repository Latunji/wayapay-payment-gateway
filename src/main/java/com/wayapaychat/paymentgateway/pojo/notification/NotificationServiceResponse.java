package com.wayapaychat.paymentgateway.pojo.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationServiceResponse {
    private String timestamp;
    private String message;
    private boolean status;
    private Object data;
}
