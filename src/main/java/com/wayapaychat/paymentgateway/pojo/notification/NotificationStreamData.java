package com.wayapaychat.paymentgateway.pojo.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStreamData {
    private String message;
    private List<NotificationReceiver> names;
}
