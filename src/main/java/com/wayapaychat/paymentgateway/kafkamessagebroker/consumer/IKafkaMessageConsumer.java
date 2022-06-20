package com.wayapaychat.paymentgateway.kafkamessagebroker.consumer;


import com.wayapaychat.paymentgateway.kafkamessagebroker.model.ProducerMessageDto;

public interface IKafkaMessageConsumer {
    @SuppressWarnings("Unchecked")
    void processMessage(ProducerMessageDto event);
}
