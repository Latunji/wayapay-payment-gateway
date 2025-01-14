package com.wayapaychat.paymentgateway.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

//@EntityListeners(value = FraudEventEntityListener.class)
@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "m_tokenized_card")
public class TokenizedCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

//    @JsonIgnore
    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "card_number")
    private String cardNumber;

//    @JsonIgnore
    @Column(name = "card_token")
    private String cardToken;

    @Column(name = "vendor")
    private String cardTokenVendor;

//    @JsonIgnore
    @Column(name = "card_token_reference")
    private String cardTokenReference;

//    @JsonIgnore
    @Column( name = "encrypted_card")
    private String encryptedCard;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dateCreated;
}
