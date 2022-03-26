package com.wayapaychat.paymentgateway.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;


@Getter
@Setter
@MappedSuperclass
public class FraudBaseEntity extends GenericBaseEntity {
    @Column(name = "ip_address")
    private String ipAddress;
    @Column(name = "email_address")
    private String emailAddress;
    @Column(name = "device_signature")
    private String deviceSignature;
    @Column(name = "phone_number")
    private String phoneNumber;
    @Column(name = "payment_response")
    private String paymentResponse;
    @Column(name = "tran_id", nullable = false)
    private String tranId;
    @Column(name = "number_of_request_made")
    private Long numberOfRequestMade;
    @Column(name = "masked_pan")
    private Boolean maskedPan;
}
