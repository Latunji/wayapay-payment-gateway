package com.wayapaychat.paymentgateway.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private String cardTokenVendor;

    @JsonIgnore
    @Column(unique = true, nullable = false, name = "card_token_reference")
    private String cardTokenReference;

    @JsonIgnore
    @Column(unique = true, nullable = false, name = "encrypted_card")
    private String encryptedCard;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dateCreated;
}
