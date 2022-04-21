package com.wayapaychat.paymentgateway.entity;

import com.wayapaychat.paymentgateway.enumm.TStatus;
import com.wayapaychat.paymentgateway.enumm.TransactionSettled;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "m_payment_wallet", uniqueConstraints = {
        @UniqueConstraint(name = "UniqueTranIdAndPaymentReferenceAndDelFlgAndDate",
                columnNames = {"tranId", "paymentReference", "del_flg", "tranDate"})})
public class PaymentWallet {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Long id;
    private boolean del_flg = false;
    private BigDecimal tranAmount;
    @Column(unique = true, nullable = false)
    private String refNo;
    private String tranId;
    private String tranDate;
    private String paymentReference;
    private String paymentDescription;
    @Enumerated(EnumType.STRING)
    private TStatus status;
    @Enumerated(EnumType.STRING)
    private TransactionSettled settled;
}
