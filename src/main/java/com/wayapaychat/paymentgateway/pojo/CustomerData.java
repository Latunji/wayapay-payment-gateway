package com.wayapaychat.paymentgateway.pojo;

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
"customerId",
"firstName",
"lastName",
"email",
"phoneNumber",
"createdAt",
"updatedAt",
"createdBy",
"updatedBy",
"merchantId",
"status",
"customerAvoided",
"dateCustomerAvoided",
"dateDeleted",
"merchantKeyMode",
"deleted"
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CustomerData {
	
	@JsonProperty("customerId")
	private String customerId;
	
	@JsonProperty("firstName")
	private String firstName;
	
	@JsonProperty("lastName")
	private String lastName;
	
	@JsonProperty("email")
	private String email;
	
	@JsonProperty("phoneNumber")
	private String phoneNumber;
	
	@JsonProperty("createdAt")
	private String createdAt;
	
	@JsonProperty("updatedAt")
	private String updatedAt;
	
	@JsonProperty("createdBy")
	private Integer createdBy;
	
	@JsonProperty("updatedBy")
	private Integer updatedBy;
	
	@JsonProperty("merchantId")
	private String merchantId;
	
	@JsonProperty("status")
	private String status;
	
	@JsonProperty("customerAvoided")
	private boolean customerAvoided;
	
	@JsonProperty("dateCustomerAvoided")
	private Object dateCustomerAvoided;
	
	@JsonProperty("dateDeleted")
	private Object dateDeleted;
	
	@JsonProperty("merchantKeyMode")
	private String merchantKeyMode;
	
	@JsonProperty("deleted")
	private boolean deleted;

}
