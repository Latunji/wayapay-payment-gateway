package com.wayapaychat.paymentgateway.common.utils;

import com.wayapaychat.paymentgateway.enumm.Permit;
import com.wayapaychat.paymentgateway.pojo.MyUserData;

import java.util.List;

public class UserPermitUtils {
    public static boolean containsAll(MyUserData myUserData, List<Permit> permits) {
        return myUserData.getPermits().containsAll(permits);
    }

    public static boolean containsAny(MyUserData myUserData, List<Permit> permits) {
        return myUserData.getPermits().stream().anyMatch(permits::contains);
    }

    public static boolean contains(MyUserData myUserData, Permit permit) {
        return myUserData.getPermits().contains(permit);
    }
}
