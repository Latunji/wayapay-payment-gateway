package com.wayapaychat.paymentgateway.pojo.waya;

import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class PaymentListResponse {

    private Date timeStamp = new Date();
    private Boolean status;
    private String message;
    private List<PaymentGateway> data;

    public PaymentListResponse() {
    }

    public PaymentListResponse(String message, List<PaymentGateway> data) {
        this.message = message;
        this.data = data;
    }

    public PaymentListResponse(Boolean status, String message, List<PaymentGateway> data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

}
