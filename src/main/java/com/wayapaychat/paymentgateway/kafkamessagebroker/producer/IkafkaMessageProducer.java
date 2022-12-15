package com.wayapaychat.paymentgateway.kafkamessagebroker.producer;

public interface IkafkaMessageProducer {
    @SuppressWarnings("unused")
    void send(String topic, Object data);
}
