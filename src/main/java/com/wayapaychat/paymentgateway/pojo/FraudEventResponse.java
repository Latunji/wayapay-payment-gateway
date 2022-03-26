package com.wayapaychat.paymentgateway.pojo;


import com.wayapaychat.paymentgateway.entity.FraudEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudEventResponse extends FraudEvent {
    private String fraudRule;
    private String fraudAction;
}
