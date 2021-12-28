package com.wayapaychat.paymentgateway.pojo;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentGatewayResponse {

	private Date timeStamp = new Date();
    private Boolean status;
    private String message;
    private Object data;

    public PaymentGatewayResponse() {
    }

    public PaymentGatewayResponse(String message, Object data) {
        this.message = message;
        this.data = data;
    }

    public PaymentGatewayResponse(Boolean status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

}
