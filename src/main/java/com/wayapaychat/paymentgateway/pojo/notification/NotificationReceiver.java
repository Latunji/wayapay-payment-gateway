package com.wayapaychat.paymentgateway.pojo.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationReceiver {
    private String email;
    private String fullName;
}
