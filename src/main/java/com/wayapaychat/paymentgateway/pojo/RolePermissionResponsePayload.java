package com.wayapaychat.paymentgateway.pojo;

import lombok.*;

import java.util.List;

@Data
public class RolePermissionResponsePayload {

    private long id;
    private String role;
    private String description;
    private List<PermissionPayload> permissions;
    private List<PermissionPayload> noPriviledge;
    private List<MerchantUserResponsePayload> users;
}
