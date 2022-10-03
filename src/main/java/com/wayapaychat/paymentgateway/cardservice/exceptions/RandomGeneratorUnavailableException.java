package com.wayapaychat.paymentgateway.cardservice.exceptions;

/**
 * Created by Omoede.Aihe on 10/17/2015.
 */
public class RandomGeneratorUnavailableException extends AbstractException {

    public RandomGeneratorUnavailableException(String code, String message) {
        super(code, message);
    }
}
