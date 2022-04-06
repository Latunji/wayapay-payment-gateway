package com.wayapaychat.paymentgateway.common.utils;

import com.wayapaychat.paymentgateway.enumm.Role;
import com.wayapaychat.paymentgateway.pojo.waya.AuthenticatedUser;

import java.util.List;

public class UserRoleUtils {
    public static boolean containsAll(AuthenticatedUser authenticatedUser, List<Role> roles) {
        return authenticatedUser.getRoles().containsAll(roles);
    }

    public static boolean containsAny(AuthenticatedUser authenticatedUser, List<Role> roles) {
        return authenticatedUser.getRoles().stream().anyMatch(roles::contains);
    }

    public static boolean contains(AuthenticatedUser authenticatedUser, Role role) {
        return authenticatedUser.getRoles().contains(role);
    }
}
