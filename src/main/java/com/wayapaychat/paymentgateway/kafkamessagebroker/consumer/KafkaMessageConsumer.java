package com.wayapaychat.paymentgateway.kafkamessagebroker.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayapaychat.paymentgateway.dao.WayaPaymentDAO;
import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.enumm.EventType;
import com.wayapaychat.paymentgateway.enumm.SettlementStatus;
import com.wayapaychat.paymentgateway.kafkamessagebroker.model.LitePaymentGateway;
import com.wayapaychat.paymentgateway.kafkamessagebroker.model.ProducerMessageDto;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Primary
@AllArgsConstructor
@Slf4j
public class KafkaMessageConsumer implements IKafkaMessageConsumer {
    private static final String TOPIC = "payment.gateway";
    private static final String WAYAQUICK_GROUP = "wayaquick";
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final PaymentGatewayRepository paymentGatewayRepository;
    private final WayaPaymentDAO wayaPaymentDAO;

    @KafkaListener(topics = {TOPIC}, groupId = WAYAQUICK_GROUP)
    public void customerSubscriptionTopicListener(String message) throws JsonProcessingException {
        log.info("--------|||| RECEIVED MESSAGE FROM KAFKA -==== TOPIC: payment.gateway||||-----------{}", message);
        processQueue(message);
    }

    private void processQueue(String event) throws JsonProcessingException {
        ProducerMessageDto producerMessageDto = objectMapper.readValue(event, ProducerMessageDto.class);
        processMessage(producerMessageDto);
    }

    @SuppressWarnings("Unchecked")
    @Override
    public void processMessage(ProducerMessageDto event) {
        EventType eventType = event.getEventCategory();
        log.info("--------|||| NOW PROCESSING MESSAGE -==== EVENT-TYPE: {} ||||-----------", eventType.toString());
        switch (eventType) {
            case TRANSACTION_SETTLED:
                log.info("--------|||| SETTLING TRANSACTIONS  ||||-----------");
                List<LitePaymentGateway> paymentGateways = objectMapper.convertValue(event.getData(), new TypeReference<>() {});
                Map<String, List<LitePaymentGateway>> grouped = paymentGateways.stream().sequential().collect(Collectors.groupingBy(LitePaymentGateway::getRefNo));
                String delimitedRefNo = paymentGateways.stream().map(litePaymentGateway -> "'" + litePaymentGateway.getRefNo() + "'").collect(Collectors.joining(","));
                List<PaymentGateway> results = wayaPaymentDAO.getAllTransactionsByRefNo(delimitedRefNo);
                results.forEach(paymentGateway -> {
                    log.info("--------|||| L.O.O.P.I.N.G  ||||-----------");
                    if (ObjectUtils.isNotEmpty(grouped.get(paymentGateway.getRefNo()))) {
                        LitePaymentGateway litePaymentGateway = grouped.get(paymentGateway.getRefNo()).get(0);
                        paymentGateway.setSettlementReferenceId(litePaymentGateway.getSettlementReferenceId());
                        paymentGateway.setSettlementDate(litePaymentGateway.getSettlementDate());
                        paymentGateway.setSettlementStatus(SettlementStatus.SETTLED);
                        log.info("--------|||| ADDED {} ||||-----------", litePaymentGateway.getSettlementReferenceId());
                    }
                });
                paymentGatewayRepository.saveAllAndFlush(results);
                log.info("--------|||| ALL SETTLED TRANSACTIONS HAVE BEEN UPDATED ||||-----------");
        }
    }
}