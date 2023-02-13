package com.wayapaychat.paymentgateway.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.wayapaychat.paymentgateway.entity.listener.FraudEventEntityListener;
import com.wayapaychat.paymentgateway.common.enums.FraudRuleType;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;

import javax.persistence.*;
import java.time.LocalDateTime;

@EntityListeners(value = FraudEventEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "m_fraud_event")
public class FraudEvent extends FraudBaseEntity {
    @Column(nullable = false, name = "fraud_action")
    private String fraudAction;

    @Column(nullable = false, name = "fraud_rule")
    private String fraudRule;

    @Column(nullable = false, name = "suspension_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime suspensionDate;

    @Column(nullable = false, name = "expired")
    private boolean expired;

    @Column(nullable = false, name = "fraud_rule_type")
    @Enumerated(EnumType.STRING)
    private FraudRuleType fraudRuleType;

    @Column(nullable = false, name = "suspension_expiry_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime suspensionExpiryDate;

    @PrePersist
    void onCreate() {
        if (ObjectUtils.isEmpty(expired))
            this.expired = false;
    }
}
