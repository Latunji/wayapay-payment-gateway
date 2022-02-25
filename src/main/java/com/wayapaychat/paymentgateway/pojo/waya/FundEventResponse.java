package com.wayapaychat.paymentgateway.pojo.waya;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"id",
"del_flg",
"posted_flg",
"tranId",
"acctNum",
"tranAmount",
"tranType",
"tranNarrate",
"tranDate",
"tranCrncyCode",
"paymentReference",
"tranCategory"
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FundEventResponse {
	
	@JsonProperty("id")
	private long id;
	
	@JsonProperty("del_flg")
	private Boolean delFlg;
	
	@JsonProperty("posted_flg")
	private Boolean postedFlg;
	
	@JsonProperty("tranId")
	private String tranId;
	
	@JsonProperty("acctNum")
	private String acctNum;
	
	@JsonProperty("tranAmount")
	private BigDecimal tranAmount;
	
	@JsonProperty("tranType")
	private String tranType;
	
	@JsonProperty("tranNarrate")
	private String tranNarrate;
	
	@JsonProperty("tranDate")
	private String tranDate;
	
	@JsonProperty("tranCrncyCode")
	private String tranCrncyCode;
	
	@JsonProperty("paymentReference")
	private String paymentReference;
	
	@JsonProperty("tranCategory")
	private String tranCategory;
	
	@JsonProperty("tranCategory")
	private String partTranType;

}
