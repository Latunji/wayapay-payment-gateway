package com.wayapaychat.paymentgateway.config;

import com.wayapaychat.paymentgateway.pojo.AuthenticatedUser;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@ToString
public class UserPrincipal implements UserDetails {

    private final AuthenticatedUser user;

    public UserPrincipal(AuthenticatedUser user) {
        this.user = user;
    }

    public static UserPrincipal create(AuthenticatedUser user) {
        return new UserPrincipal(user);
    }

    public Long getId() {
        return user.getId();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> grantedAuthorities = this.user.getRoles().stream()
                .map(Enum::name)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        grantedAuthorities.addAll(
                this.user.getPermits().stream()
                        .map(Enum::name)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet())
        );
        return grantedAuthorities;
    }

    public AuthenticatedUser getUser() {
        return this.user;
    }

    @Override
    public String getUsername() {
        return user.getEmail() != null ? this.user.getEmail() : this.user.getPhoneNumber();
    }

}
