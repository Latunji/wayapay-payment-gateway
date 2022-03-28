package com.wayapaychat.paymentgateway.exception;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {

    private final String code;
    @JsonIgnore
    private final int httpStatus;

    public ApplicationException(int httpStatus, String code, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public ApplicationException(int httpStatus, String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
