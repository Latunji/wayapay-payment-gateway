package com.wayapaychat.paymentgateway.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.wayapaychat.paymentgateway.entity.listener.FraudEventEntityListener;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import java.time.LocalDateTime;

@EntityListeners(value = FraudEventEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@Entity
//@Table(name = "m_recurrent_payment")
public class RecurrentPayment extends GenericBaseEntity {
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dateFirstPaymentMade;

    @Column(nullable = false)
    private String paymentLinkId;

    @Column(nullable = false)
    private String customerId;

    @JsonIgnore
    @Column(nullable = false)
    private String encryptedCard;
}
