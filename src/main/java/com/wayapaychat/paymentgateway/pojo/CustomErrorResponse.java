package com.wayapaychat.paymentgateway.pojo;

import com.wayapaychat.paymentgateway.enumm.Constants;
import org.apache.logging.log4j.util.Strings;


public class CustomErrorResponse extends ResponseHelper {

    public CustomErrorResponse(String message) {
        super(false, message, Strings.EMPTY);
    }

    public CustomErrorResponse(String message, Object data) {
        super(false, message, data);
    }

    public CustomErrorResponse() {
        super(false, Constants.ERROR_MESSAGE, Strings.EMPTY);
    }

}
