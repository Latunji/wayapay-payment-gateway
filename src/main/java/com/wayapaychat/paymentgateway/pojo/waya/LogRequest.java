package com.wayapaychat.paymentgateway.pojo.waya;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogRequest {

    private Long id;
    private String action;
    private String jsonRequest;
    private String jsonResponse;
    private String message;
    private String module;
    private String requestDate;
    private String responseDate;
    private Long userId;

    public LogRequest() {
        this.requestDate = LocalDateTime.now().toString();
        this.responseDate = LocalDateTime.now().toString();
    }
}
