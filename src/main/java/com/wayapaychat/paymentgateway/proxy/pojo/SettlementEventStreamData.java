package com.wayapaychat.paymentgateway.proxy.pojo;

import com.wayapaychat.paymentgateway.enumm.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementEventStreamData {
    private EventType eventType;
    private String merchantId;
    private String defaultWalletCredited;
    private Long userId;
    private BigDecimal amountSettled;
    private Date dateSettlementMade;
    private String transactionsSettled;
    private String officialAccountDebited;
}
