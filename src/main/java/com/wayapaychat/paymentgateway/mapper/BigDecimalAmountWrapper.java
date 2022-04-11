package com.wayapaychat.paymentgateway.mapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BigDecimalAmountWrapper {
    private BigDecimal amount;
}
