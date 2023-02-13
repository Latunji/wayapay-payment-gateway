package com.wayapaychat.paymentgateway.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayapaychat.paymentgateway.common.enums.FraudRuleType;
import com.wayapaychat.paymentgateway.entity.*;
import com.wayapaychat.paymentgateway.repository.FraudEventRepository;
import com.wayapaychat.paymentgateway.repository.FraudRuleRepository;
import com.wayapaychat.paymentgateway.repository.FraudTrackerRepository;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import com.wayapaychat.paymentgateway.service.FraudTrackerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class FraudTrackerImpl implements FraudTrackerService {
    private final FraudTrackerRepository fraudTrackerRepository;
    private final FraudEventRepository fraudEventRepository;
    private final FraudRuleRepository fraudRuleRepository;
    private PaymentGatewayRepository paymentGatewayRepository;
//    private final ObjectMapper objectMapper;

    @Scheduled(cron = "* 60 * * * *") // 60min
    @SchedulerLock(name = "TaskScheduler_checkFraudSchedulerEveryHour", lockAtLeastFor = "10s", lockAtMostFor = "30s")
    public void checkFraudSchedulerEveryHour() {
        log.info("----------------------------- Starting FRAUD SCHEDULE -----------------------------");
        checkPaymentsFromSameIpAddress();
    }

    private void checkPaymentsFromSameIpAddress() {
        log.info("----------------------------- CHECKING MULTIPLE PAYMENTS FROM SAME IP ADDRESS -----------------------------");

        LocalDateTime anHourAgo = LocalDateTime.now().minusHours(1L);
        LocalDateTime justNow = LocalDateTime.now();
        List<PaymentGateway> allPayment = paymentGatewayRepository.findAllPaymentsWithinTheHour(anHourAgo, justNow);
        log.info("---- FOUND "+allPayment.size()+" TRANSACTIONS WITHIN THE LAST HOUR");
        List<PaymentGateway> allIPAddress =  allPayment.stream().filter(distinctByKey(PaymentGateway::getCustomerIpAddress)).collect(Collectors.toList());

        for(PaymentGateway p : allIPAddress){
            List<PaymentGateway> totalCount = paymentGatewayRepository.findByCustomerIpAddress(p.getCustomerIpAddress());
            if(totalCount.size() >= 3){
                FraudEvent fraudEvent = new FraudEvent();

                fraudEvent.setIpAddress(p.getCustomerIpAddress());
                fraudEvent.setEmailAddress(p.getCustomerEmail());
                fraudEvent.setMaskedPan(p.getMaskedPan());
                fraudEvent.setPaymentResponse(p.getStatus().toString());
                fraudEvent.setNumberOfRequestMade(3L);
                fraudEvent.setPhoneNumber(p.getCustomerPhone());
                fraudEvent.setTranId(p.getTranId());
                fraudEvent.setDeleted(false);
                fraudEvent.setFraudAction(FraudRuleType.SAME_IP_THREE_TIMES_TRANSACTION_IN_ONE_HOUR.getAction());
                fraudEvent.setFraudRule(FraudRuleType.SAME_IP_THREE_TIMES_TRANSACTION_IN_ONE_HOUR.getRule());
                fraudEvent.setSuspensionDate(LocalDateTime.now());
                fraudEvent.setSuspensionExpiryDate(LocalDateTime.now().plusHours(1L));
                fraudEvent.setFraudRuleType(FraudRuleType.SAME_IP_THREE_TIMES_TRANSACTION_IN_ONE_HOUR);
                fraudEvent.setExpired(false);

                fraudEventRepository.save(fraudEvent);
            }
        }
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }


    @Scheduled(cron = "*/59 * * * * *")// EveryMin
    @SchedulerLock(name = "TaskScheduler_checkFraudSchedulerEveryHour", lockAtLeastFor = "10s", lockAtMostFor = "30s")
    public void checkExpiredFraudSchedulerEveryMinute() {
        log.info("----------------------------- Starting FRAUD EXPIRATION SCHEDULE -----------------------------");
        checkExpiredFraudEvents();
    }

    private void checkExpiredFraudEvents() {
            List<FraudEvent> f = fraudEventRepository.findAllNotExpired();
            f.stream().filter(b -> b.getSuspensionExpiryDate().equals(LocalDateTime.now())).forEach(t ->{
                t.setExpired(true);
                fraudEventRepository.save(t);
            });
    }

}
