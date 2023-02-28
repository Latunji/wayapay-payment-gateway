package com.wayapaychat.paymentgateway.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericApiProxyResponse<R> {
    private String code;
    private String date;
    private String message;
    private R data;
}
