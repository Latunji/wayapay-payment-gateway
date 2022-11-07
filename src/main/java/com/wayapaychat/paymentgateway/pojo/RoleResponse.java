package com.wayapaychat.paymentgateway.pojo;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RoleResponse {
    private Date timestamp = new Date();
    private String message;
    private boolean status;
    private UserRoleInvite data;
}
