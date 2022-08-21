package com.wayapaychat.paymentgateway.kafkamessagebroker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipientsSMS {

    private String name;
    private String telephone;
}
