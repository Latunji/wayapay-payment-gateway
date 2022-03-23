package com.wayapaychat.paymentgateway.entity;


import com.wayapaychat.paymentgateway.entity.listener.PaymentGatewayEntityListener;
import com.wayapaychat.paymentgateway.entity.listener.PaymentGatewayFraudEventEntityListener;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

@Data
@EntityListeners(value = PaymentGatewayFraudEventEntityListener.class)
@Builder
@Entity
@NoArgsConstructor
@Table(name = "m_payment_gateway_fraud_event")
public class PaymentGatewayFraudEvent extends BaseEntity {
}
