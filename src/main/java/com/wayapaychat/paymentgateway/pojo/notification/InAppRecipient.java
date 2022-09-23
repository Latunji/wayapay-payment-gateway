package com.wayapaychat.paymentgateway.pojo.notification;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InAppRecipient {
    @NotBlank(message = "value must not be blank, also enter the right key *userId*")
    private String userId;
}
