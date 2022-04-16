package com.wayapaychat.paymentgateway.entity;


import com.wayapaychat.paymentgateway.enumm.PaymentChannel;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "m_settlement_pricing_configuration", indexes = {@Index(name = "ix__m_settlement_pricing_configuration__col_chanel_uq", columnList = "channel")})
public class SettlementPricingConfiguration extends GenericBaseEntity {
    @Column(name = "pricing_amount")
    private BigDecimal pricingAmount;
    @Column(name = "channel", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentChannel channel;
    @Column(name = "reference_code", unique = true, nullable = false)
    private String referenceCode;
    @Column(name = "description")
    private String description;

    @PrePersist
    void prePersist() {
        this.setDateCreated(LocalDateTime.now());
        this.setCreatedBy(0L);
        if (ObjectUtils.isEmpty(referenceCode))
            referenceCode = "SET_CONF_" + RandomStringUtils.randomAlphanumeric(5)
                    + System.currentTimeMillis()
                    + RandomStringUtils.randomAlphanumeric(5);
    }
}
