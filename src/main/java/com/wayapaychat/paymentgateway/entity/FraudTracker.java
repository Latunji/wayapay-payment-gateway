package com.wayapaychat.paymentgateway.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.wayapaychat.paymentgateway.entity.listener.FraudRuleEntityListener;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import javax.persistence.*;
import java.time.LocalDateTime;

@EntityListeners(value = FraudRuleEntityListener.class)
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "m_fraud_tracker")
public class FraudTracker extends FraudBaseEntity {
    @JsonIgnore
    @Column(name = "hashed_pan")
    private String hashedPan;

    @Column(name = "violated")
    private Boolean violated;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dateViolated;

    @PrePersist
    void onCreate() {
        if (ObjectUtils.isEmpty(violated))
            violated = false;
    }
}
