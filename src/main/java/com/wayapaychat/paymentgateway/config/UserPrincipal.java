package com.wayapaychat.paymentgateway.config;

import com.wayapaychat.paymentgateway.pojo.MyUserData;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@ToString
public class UserPrincipal implements UserDetails {

    private final MyUserData user;

    public UserPrincipal(MyUserData user) {
        this.user = user;
    }

    public static UserPrincipal create(MyUserData user) {
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
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        grantedAuthorities.addAll(
                this.user.getPermits().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet())
        );
        return grantedAuthorities;
    }

    public MyUserData getUser() {
        return this.user;
    }

    @Override
    public String getUsername() {
        return user.getEmail() != null ? this.user.getEmail() : this.user.getPhoneNumber();
    }

}
