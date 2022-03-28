package com.wayapaychat.paymentgateway.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wayapaychat.paymentgateway.enumm.Permit;
import com.wayapaychat.paymentgateway.enumm.Role;
import lombok.*;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MyUserData {
    private Long id;
    private String email;
    private String phoneNumber;
    private String referenceCode;
    private String firstName;
    private String surname;
    private String password;
    private boolean phoneVerified;
    private boolean emailVerified;
    private boolean pinCreated;
    private boolean corporate;
    private Boolean admin;
    private List<Role> roles;
    private List<Permit> permits;
    private String transactionLimit;
    public MyUserData(String email) {
        this.email = email;
    }
}
