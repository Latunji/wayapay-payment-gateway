package com.wayapaychat.paymentgateway.utils;

import com.wayapaychat.paymentgateway.enumm.Role;
import com.wayapaychat.paymentgateway.pojo.MyUserData;

import java.util.List;

public class UserRoleUtils {
    public static boolean containsAll(MyUserData myUserData, List<Role> roles) {
        return myUserData.getRoles().containsAll(roles);
    }

    public static boolean containsAny(MyUserData myUserData, List<Role> roles) {
        return myUserData.getRoles().stream().anyMatch(roles::contains);
    }

    public static boolean contains(MyUserData myUserData, Role role) {
        return myUserData.getRoles().contains(role);
    }
}
