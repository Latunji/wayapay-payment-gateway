package com.wayapaychat.paymentgateway.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.wayapaychat.paymentgateway.entity.listener.PaymentGatewayEntityListener;
import com.wayapaychat.paymentgateway.enumm.PaymentChannel;
import com.wayapaychat.paymentgateway.enumm.TransactionStatus;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@EntityListeners(value = PaymentGatewayEntityListener.class)
@Builder
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "m_payment_gateway")
@TypeDef(typeClass = JsonBinaryType.class, name = "JSONB")
public class PaymentGateway {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Long id;
    private boolean del_flg = false;
    @Column(unique = true, nullable = false)
    private String refNo;
    @Column(unique = true, nullable = false)
    private String tranId;
    @Column(nullable = false)
    private String merchantId;
    private String description;
    private BigDecimal amount;
    private BigDecimal fee;
    private String currencyCode;
    private String returnUrl;
    @Column(nullable = false)
    private String secretKey; // hash
    private String scheme;
    private String cardNo; // hash
    private String mobile;
    //private String status;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    @Column(columnDefinition = "TEXT")
    private String encyptCard;
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate tranDate;
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime rcre_time;
    private String preferenceNo;
    private boolean successfailure;
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate vendorDate;
    private String merchantName;
    private String customerName;
    private String customerEmail;
    private String merchantEmail;
    private String customerPhone;
    @Enumerated(EnumType.STRING)
    private PaymentChannel channel;
    private boolean tranflg;
    @Column(name = "customer_ip_address", columnDefinition = "VARCHAR(100)")
    private String customerIpAddress;
    @Type(type = "JSONB")
    @Column(name = "payment_meta_data", columnDefinition = "JSONB")
    private String paymentMetaData;
}
