package com.wayapaychat.paymentgateway.aspect;


import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class PaymentGateWayAspect {

        @Before("* * com.wayapaychat.paymentgateway.entity.RecurrentPayment")
        public void checkingAspect(){

        }
}