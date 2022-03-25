package com.wayapaychat.paymentgateway.entity;

import com.wayapaychat.paymentgateway.entity.listener.FraudRuleEntityListener;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import javax.persistence.*;

@EntityListeners(value = FraudRuleEntityListener.class)
@Data
@Entity
@NoArgsConstructor
@Table(name = "m_fraud_rule")
public class FraudRule extends GenericBaseEntity {
    @Column(name = "rule_content", nullable = false)
    private String ruleContent;
    @Column(name = "rule_action", nullable = false)
    private String ruleAction;
    @Column(name = "rule_is_active", nullable = false)
    private Boolean ruleIsActive;

    @PrePersist
    void onCreate() {
        if (ObjectUtils.isEmpty(ruleIsActive))
            this.ruleIsActive = false;
    }
}
