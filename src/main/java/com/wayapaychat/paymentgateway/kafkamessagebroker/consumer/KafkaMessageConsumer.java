package com.wayapaychat.paymentgateway.kafkamessagebroker.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayapaychat.paymentgateway.enumm.EventType;
import com.wayapaychat.paymentgateway.kafkamessagebroker.model.ProducerMessageDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@Primary
@AllArgsConstructor
@Slf4j
public class KafkaMessageConsumer implements IKafkaMessageConsumer {
    private static final String TOPIC = "merchant";
    private static final String GROUP = "waya";
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {TOPIC}, groupId = GROUP)
    public void customerSubscriptionTopicListener(String message) throws JsonProcessingException {
        processQueue(message);
        log.info("--------||||RECEIVED MESSAGE FROM KAFKA||||-----------{}", message);
    }

    private void processQueue(String event) throws JsonProcessingException {
        ProducerMessageDto producerMessageDto = objectMapper.readValue(event, ProducerMessageDto.class);
        processMessage(producerMessageDto);
    }

    @SuppressWarnings("Unchecked")
    @Override
    public void processMessage(ProducerMessageDto event) {
        EventType eventType = event.getEventCategory();
        System.out.println(eventType);
    }
}