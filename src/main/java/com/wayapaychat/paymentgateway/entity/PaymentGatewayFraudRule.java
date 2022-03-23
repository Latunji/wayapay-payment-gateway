package com.wayapaychat.paymentgateway.entity;

import com.wayapaychat.paymentgateway.entity.listener.PaymentGatewayFraudRuleEntityListener;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

@EntityListeners(value = PaymentGatewayFraudRuleEntityListener.class)
@Data
@Builder
@Entity
@NoArgsConstructor
@Table(name = "m_payment_gateway_fraud_rule")
public class PaymentGatewayFraudRule extends BaseEntity {
}
