package com.wayapaychat.paymentgateway.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "m_processor_configuration")
public class ProcessorConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    @JsonIgnore
    private Long id;

    @Column(name = "card_acquiring")
    private String cardAcquiring;

    @Column(name = "ussd_acquiring")
    private String ussdAcquiring;

    @Column(name = "account_acquiring")
    private String accountAcquiring;

    @Column(name = "payattitude_acquiring")
    private String payattitudeAcquiring;
}
