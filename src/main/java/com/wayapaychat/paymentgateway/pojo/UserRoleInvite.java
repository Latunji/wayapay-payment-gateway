package com.wayapaychat.paymentgateway.pojo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserRoleInvite {

        private Long id;
        private Long inviteeUserId;
        private Long managerId;
        private boolean isCorporate;
        private String permissionName;
        private BigDecimal transactionLimit = new BigDecimal("0.00");
        private String roleName;
        private String inviteeName;
//    private String inviteeEmail;
//    private String inviteePhoneNumber;
}
