package com.wayapaychat.paymentgateway.pojo.waya;

import lombok.Data;

import java.util.Date;

@Data
public class ResponseHelper {
    private Date timeStamp = new Date();
    private Boolean status;
    private String message;
    private Object data;

    public ResponseHelper(Boolean status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
