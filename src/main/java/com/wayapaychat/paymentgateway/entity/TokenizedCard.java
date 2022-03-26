package com.wayapaychat.paymentgateway.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.Column;

//@EntityListeners(value = FraudEventEntityListener.class)
@Builder
//@Entity
@NoArgsConstructor
@AllArgsConstructor
//@Table(name = "m_tokenized_card")
public class TokenizedCard extends GenericBaseEntity {
    @JsonIgnore
    @Column(unique = true, nullable = false, name = "customer_id")
    private String customerId;

    @Column(unique = true, nullable = false, name = "merchant_id")
    private String merchantId;

    @JsonIgnore
    @Column(nullable = false, name = "card_token")
    private String cardToken;

    @JsonIgnore
    @Column(unique = true, nullable = false, name = "card_token_reference")
    private String cardTokenReference;

    @JsonIgnore
    @Column(unique = true, nullable = false, name = "encrypted_card")
    private String encryptedCard;
}
