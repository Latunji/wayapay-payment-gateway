package com.wayapaychat.paymentgateway.entity;


import com.wayapaychat.paymentgateway.entity.listener.PaymentGatewayEntityListener;
import lombok.*;

import javax.persistence.*;

@EntityListeners(value = PaymentGatewayEntityListener.class)
@Builder
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "m_payment_gateway_fraud_event")
public class PaymentGatewayFraudEvent extends BaseEntity{
}
