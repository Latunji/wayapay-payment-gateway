package com.wayapaychat.paymentgateway.pojo.waya;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class Customer {

    @NotBlank(message = "Name must not be null")
    @Size(min = 1, max = 100, message = "The name '${validatedValue}' must be between {min} and {max} characters long")
    private String name;

    @NotBlank(message = "Email must not be null")
    @Size(min = 11, max = 50, message = "The email '${validatedValue}' must be between {min} and {max} characters long")
    @Email
    private String email;

    @NotBlank(message = "Phone Number must not be null")
    @Size(min = 3, max = 20, message = "The phoneNumber '${validatedValue}' must be between {min} and {max} characters long")
    private String phoneNumber;
    private String customerId;

    public Customer(String name, String email, String phoneNumber, String customerId) {
        super();
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.customerId = customerId;
    }

    public Customer() {
        super();

    }


}
