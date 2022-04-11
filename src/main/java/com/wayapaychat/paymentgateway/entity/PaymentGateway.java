package com.wayapaychat.paymentgateway.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.wayapaychat.paymentgateway.common.enums.MerchantTransactionMode;
import com.wayapaychat.paymentgateway.entity.listener.PaymemtGatewayEntityListener;
import com.wayapaychat.paymentgateway.enumm.AccountSettlementOption;
import com.wayapaychat.paymentgateway.enumm.PaymentChannel;
import com.wayapaychat.paymentgateway.enumm.TransactionStatus;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@EntityListeners(value = PaymemtGatewayEntityListener.class)
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
    private BigDecimal fee = BigDecimal.ZERO;
    private String currencyCode;
    private String returnUrl;
    @Column(nullable = false)
    @JsonIgnore
    private String secretKey; // hash
    private String scheme;
    private String cardNo; // hash
    private String mobile;
    //private String status;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    private String encyptCard;
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate tranDate;
    @Column(name = "settlement_date", columnDefinition = "TIMESTAMPTZ DEFAULT NULL")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate settlementDate;
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
    private String customerId;
    private boolean tranflg;
    @Column(name = "customer_ip_address", columnDefinition = "VARCHAR(100)")
    private String customerIpAddress;
    @Type(type = "JSONB")
    @Column(name = "payment_meta_data", columnDefinition = "JSONB")
    private String paymentMetaData;
    @Column(name = "masked_pan")
    private String maskedPan;
    @Column(name = "payment_link")
    private String paymentLinkId;
    @Column(name = "recurrent_payment_id")
    private Long recurrentPaymentId;
    @Column(name = "is_from_recurrent_payment")
    private Boolean isFromRecurrentPayment;
    @Enumerated(EnumType.STRING)
    @Column(name = "mode", columnDefinition = "VARCHAR(255)")
    private MerchantTransactionMode mode;
    @Column(name = "session_id")
    private String sessionId;
    @Column(name = "account_settled_to")
    private String accountSettledTo; //account number settled to
    @Column(name = "account_settlement_option")
    @Enumerated(EnumType.STRING)
    private AccountSettlementOption accountSettlementOption; // WALLET or BANK
    @JsonIgnore
    @Column(name = "processing_fee")
    private BigDecimal processingFee = BigDecimal.ZERO; //Third party processing fee
    @JsonIgnore
    @Column(name = "wayapay_fee")
    private BigDecimal wayapayFee = BigDecimal.ZERO;

    @PrePersist
    void prePersist() {
        if (ObjectUtils.isEmpty(isFromRecurrentPayment))
            isFromRecurrentPayment = false;
        fee = wayapayFee.add(processingFee);
    }
}
