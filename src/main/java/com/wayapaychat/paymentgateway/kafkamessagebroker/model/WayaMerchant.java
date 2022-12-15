package com.wayapaychat.paymentgateway.kafkamessagebroker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class WayaMerchant {
    private String merchantId;
    private String merchantCallbackURL;
    private String merchantWebHookURL;
    private String merchantKeyMode;
    private String createdAt;
    private String updatedAt;
    private Long modifyBy;
    private Long createdBy;
    private Long userId;
    private Boolean isDeleted;
    private Long deletedBy;
    private String dateDeleted;
    private Boolean accountActive;
    private String dateDeactivated;
    private Long deactivatedBy;
    private String deactivationReason;
    private String merchantEmailAddress;
    private String merchantPhoneNumber;
    private String merchantName;
    private Boolean hasPricing;
}
