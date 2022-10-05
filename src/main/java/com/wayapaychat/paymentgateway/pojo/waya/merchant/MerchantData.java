package com.wayapaychat.paymentgateway.pojo.waya.merchant;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantData {
	
	private String merchantId;
	private String refNo;
	private String merchantSecretKey;
	private String merchantSecretTestKey;
	private String merchantPublicTestKey;
	private String merchantProductionSecretKey;
	private String merchantProductionPublicKey;
	private String merchantCallbackURL;
	private String merchantWebHookURL;
	private String merchantKeyMode;
	private String createdAt;
	private String updatedAt;
	private String modifyBy;
	private long createdBy;
	private long userId;
    private boolean isDeleted;
    private String deletedBy;
    private String dateDeleted;
    private boolean accountActive;
    private String dateDeactivated;
    private String deactivatedBy;
    private String deactivationReason;
    private String activePublicKey;
    private String activeSecretKey;
	private String merchantEmailAddress;
	private String merchantPhoneNumber;
}
