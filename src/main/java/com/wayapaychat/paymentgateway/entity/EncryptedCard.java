package com.wayapaychat.paymentgateway.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wayapaychat.paymentgateway.entity.listener.FraudEventEntityListener;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EntityListeners;

@EntityListeners(value = FraudEventEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@Entity
//@Table(name = "m_fraud_event")
public class EncryptedCard extends GenericBaseEntity {
    @JsonIgnore
    @Column(unique = true, nullable = false, name = "encrypted_card")
    private String encryptedCard;
    @JsonIgnore
    @Column(name = "encrypted_card_reference")
    private String encryptedCardReference;
}
