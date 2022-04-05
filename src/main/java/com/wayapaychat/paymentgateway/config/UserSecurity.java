package com.wayapaychat.paymentgateway.config;


import com.wayapaychat.paymentgateway.pojo.AuthenticatedUser;
import com.wayapaychat.paymentgateway.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collection;

@SuppressWarnings("ALL")
@Component("userSecurity")
@Slf4j
public class UserSecurity {

    private final UserService userService;
    public UserSecurity(UserService userService) {
        this.userService = userService;
    }

    public boolean isCorporate(boolean isCorporate, Authentication authentication) {
        AuthenticatedUser user = ((AuthenticatedUser) authentication.getPrincipal());
        return user.isCorporate() && isCorporate;
    }

    private boolean roleCheck(Collection<String> roleList, String role) {
        return roleList.contains(role);
    }

}
