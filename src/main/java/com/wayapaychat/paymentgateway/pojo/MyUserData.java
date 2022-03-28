package com.wayapaychat.paymentgateway.pojo;

import java.util.List;

import com.wayapaychat.paymentgateway.enumm.Permit;
import com.wayapaychat.paymentgateway.enumm.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
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
    private boolean isAdmin;
    private List<Role> roles;
    private List<Permit> permits;
    private String transactionLimit;
    
    public MyUserData(String email) {
    	this.email = email;
    }

}
