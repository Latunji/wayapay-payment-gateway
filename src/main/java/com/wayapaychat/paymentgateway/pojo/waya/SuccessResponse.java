package com.wayapaychat.paymentgateway.pojo.waya;

import com.wayapaychat.paymentgateway.common.enums.Constant;

public class SuccessResponse extends PaymentGatewayResponse {
	
    public SuccessResponse(String message, Object data) {
        super(true, message, data);
    }

    public SuccessResponse(Object data) {
        super(true, Constant.OPERATION_SUCCESS, data);
    }
}
