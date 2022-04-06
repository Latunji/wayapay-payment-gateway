package com.wayapaychat.paymentgateway.pojo.waya;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"id",
"email",
"firstName",
"surname",
"dateOfBirth",
"gender",
"phoneNumber",
"userId",
"referral",
"referenceCode",
"smsAlertConfig",
"corporate",
"otherDetails"
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileData {
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("email")
	private String email;
	
	@JsonProperty("firstName")
	private String firstName;
	
	@JsonProperty("surname")
	private String surname;
	
	@JsonProperty("dateOfBirth")
	private String dateOfBirth;
	
	@JsonProperty("gender")
	private String gender;
	
	@JsonProperty("phoneNumber")
	private String phoneNumber;
	
	@JsonProperty("userId")
	private String userId;
	
	@JsonProperty("referral")
	private String referral;
	
	@JsonProperty("referenceCode")
	private String referenceCode;
	
	@JsonProperty("smsAlertConfig")
	private Boolean smsAlertConfig;
	
	@JsonProperty("corporate")
	private Boolean corporate;
	
	@JsonProperty("otherDetails")
	private OtherDetails otherDetails;

}
