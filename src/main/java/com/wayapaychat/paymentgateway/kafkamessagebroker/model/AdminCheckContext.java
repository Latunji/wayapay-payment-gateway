package com.wayapaychat.paymentgateway.kafkamessagebroker.model;


import com.wayapaychat.paymentgateway.kafkamessagebroker.AbstractEmailContext;

public class AdminCheckContext extends AbstractEmailContext {

    @Override
    public <T> void init(T context) {
        // we can do any common configuration setup here
        // like setting up some base URL and context
//        Users customer = (Users) context; // we pass the customer information
//        put("firstName", customer.getFirstName());
//        put("requestType", "Admin Action");
//        setTemplateLocation("emails/admin-check");
//        setSubject("Confirm Admin Action !!!");
//        setEmail(customer.getEmail());
//        setDisplayName(customer.getFirstName());
//        setTo(customer.getEmail());
    }
}
