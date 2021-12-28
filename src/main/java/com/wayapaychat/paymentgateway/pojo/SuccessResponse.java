package com.wayapaychat.paymentgateway.pojo;

import com.wayapaychat.paymentgateway.utils.Constant;

public class SuccessResponse extends PaymentGatewayResponse {
	
    public SuccessResponse(String message, Object data) {
        super(true, message, data);
    }

    public SuccessResponse(Object data) {
        super(true, Constant.OPERATION_SUCCESS, data);
    }
}
