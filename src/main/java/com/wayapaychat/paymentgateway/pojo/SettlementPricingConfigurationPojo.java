package com.wayapaychat.paymentgateway.pojo;


import com.wayapaychat.paymentgateway.enumm.PaymentChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementPricingConfigurationPojo {
    @NotNull
    private PaymentChannel channel;
    @NotNull
    private BigDecimal pricingAmount;
}
