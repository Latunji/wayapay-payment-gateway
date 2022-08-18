package com.wayapaychat.paymentgateway.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "m_processor_configuration")
public class ProcessorConfiguration {
    @Column(name = "card_acquiring")
    private String cardAcquiring;

    @Column(name = "ussd_acquiring")
    private String ussdAcquiring;

    @Column(name = "account_acquiring")
    private String accountAcquiring;

    @Column(name = "payattitude_acquiring")
    private String payattitudeAcquiring;
}
