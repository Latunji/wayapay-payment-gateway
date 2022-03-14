package com.wayapaychat.paymentgateway.pojo;

import com.wayapaychat.paymentgateway.enumm.PaymentChannel;
import com.wayapaychat.paymentgateway.enumm.TransactionMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationPojo {
    private PaymentChannel paymentChannel;
    private String merchantName;
    private String merchantEmailAddress;
    private BigDecimal transactionAmount;
    private LocalDate transactionDate;
    private String currency;
    private String transactionMode;
    private LocalDate updatedAt;
    private String transactionNarration;
    private String customerId;
    private String customerEmailAddress;
    private String customerName;
    private String paymentGatewayTransactionId;
    private String channelTransactionId;
}
