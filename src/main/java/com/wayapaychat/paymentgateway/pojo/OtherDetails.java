package com.wayapaychat.paymentgateway.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"organisationName",
"organisationEmail",
"organisationPhone",
"organizationCity",
"organizationAddress",
"organizationState",
"businessType"
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtherDetails {
	
	@JsonProperty("organisationName")
	private String organisationName;
	
	@JsonProperty("organisationEmail")
	private String organisationEmail;
	
	@JsonProperty("organisationPhone")
	private String organisationPhone;
	
	@JsonProperty("organizationCity")
	private String organizationCity;
	
	@JsonProperty("organizationAddress")
	private String organizationAddress;
	
	@JsonProperty("organizationState")
	private String organizationState;
	
	@JsonProperty("businessType")
	private String businessType;

}
