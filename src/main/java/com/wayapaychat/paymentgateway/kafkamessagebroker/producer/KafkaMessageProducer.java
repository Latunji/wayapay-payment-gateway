package com.wayapaychat.paymentgateway.kafkamessagebroker.producer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
@Service
public class KafkaMessageProducer implements IkafkaMessageProducer {
    @Autowired
    private KafkaTemplate<String, Object> template;

    @SuppressWarnings("unused")
    @Override
    public void send(String topic, Object data) {
        ListenableFuture<SendResult<String, Object>> future = template.send(topic, data);
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, Object> result) {
                log.info("Success:: notification sent to the event queue");
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("Unable to send message=[" + data + "] due to : {}", ex.getMessage());
                log.error("Full Error: {0}", ex);
            }
        });
    }
}