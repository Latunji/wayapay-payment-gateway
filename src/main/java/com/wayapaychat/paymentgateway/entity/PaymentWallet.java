package com.wayapaychat.paymentgateway.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "m_payment_wallet" , uniqueConstraints = {
        @UniqueConstraint(name = "UniqueTranIdAndPaymentReferenceAndDelFlgAndDate", 
        		columnNames = {"tranId", "paymentReference", "del_flg","tranDate"})})
public class PaymentWallet {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	private Long id;

	private boolean del_flg = false;
	
	private BigDecimal tranAmount;
	
	private String tranId;
	
	private String tranDate;
	
	private String paymentReference;
	
	private String paymentDescription;

}
