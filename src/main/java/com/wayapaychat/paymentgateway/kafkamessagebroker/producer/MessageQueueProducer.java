package com.wayapaychat.paymentgateway.kafkamessagebroker.producer;

public interface MessageQueueProducer {
    @SuppressWarnings("unused")
    void send(String topic, Object data);
}
