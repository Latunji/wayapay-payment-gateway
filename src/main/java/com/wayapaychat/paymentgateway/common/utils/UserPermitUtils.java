package com.wayapaychat.paymentgateway.common.utils;

import com.wayapaychat.paymentgateway.enumm.Permit;
import com.wayapaychat.paymentgateway.pojo.waya.AuthenticatedUser;

import java.util.List;

public class UserPermitUtils {
    public static boolean containsAll(AuthenticatedUser authenticatedUser, List<Permit> permits) {
        return authenticatedUser.getPermits().containsAll(permits);
    }

    public static boolean containsAny(AuthenticatedUser authenticatedUser, List<Permit> permits) {
        return authenticatedUser.getPermits().stream().anyMatch(permits::contains);
    }

    public static boolean contains(AuthenticatedUser authenticatedUser, Permit permit) {
        return authenticatedUser.getPermits().contains(permit);
    }
}
