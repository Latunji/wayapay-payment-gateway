package com.wayapaychat.paymentgateway.cardservice.exceptions;

public class ConflictException extends AbstractException {

    public ConflictException(String code, String message) {
        super(code, message);

    }
}
