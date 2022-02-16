package com.wayapaychat.paymentgateway.pojo;


import com.wayapaychat.paymentgateway.utils.Constant;

public class ErrorResponse extends PaymentGatewayResponse {

    public ErrorResponse(String message){
        super(false, message, null);
    }

    public ErrorResponse(){
        super(false, Constant.ERROR_PROCESSING, null);
    }
}
