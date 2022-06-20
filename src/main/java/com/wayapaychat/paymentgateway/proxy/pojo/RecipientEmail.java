package com.wayapaychat.paymentgateway.proxy.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipientEmail {
    @Email(message = "email must be valid")
    private String email;
}
