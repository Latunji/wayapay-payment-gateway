package com.wayapaychat.paymentgateway.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "card_charged")
@Data
public class ChargedCard implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @Column(name = "account_number")
    private String accountNumber;
    @Column(name = "card_number")
    private String cardNumber;
    @Column(name = "customer_id")
    private String customerId;
    @Column(name = "trans_ref")
    private String transRef;
    @Column(name = "amount")
    private String amount;


}
