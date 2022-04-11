package com.wayapaychat.paymentgateway.cron;

import com.wayapaychat.paymentgateway.common.utils.PaymentGateWayCommonUtils;
import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.pojo.waya.FundEventResponse;
import com.wayapaychat.paymentgateway.proxy.AuthApiClient;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import com.wayapaychat.paymentgateway.repository.RecurrentTransactionRepository;
import com.wayapaychat.paymentgateway.service.PaymentGatewayService;
import com.wayapaychat.paymentgateway.service.impl.UnifiedPaymentProxy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MerchantTransactionSettlementCronService {

    @Autowired
    PaymentGatewayRepository paymentGatewayRepo;
    @Autowired
    PaymentGateWayCommonUtils paymentGateWayCommonUtils;
    @Autowired
    PaymentGatewayService paymentService;
    @Autowired
    UnifiedPaymentProxy uniPayProxy;
    @Autowired
    AuthApiClient authProxy;
    @Autowired
    private RecurrentTransactionRepository recurrentTransactionRepository;
    @Value("${service.name}")
    private String username;

    @Value("${service.pass}")
    private String passSecret;

    @Scheduled(cron = "0 0 0 * * *")
    private void settleTransactionEveryDay() {
//        processFundMerchantWalletAccount();
    }

    @Scheduled(cron = "*/5 * * * * *")
    private void settleEveryFiveSeconds() {
//        processFundMerchantWalletAccount();
    }

    @Scheduled(cron = "0 0 0 28-31 JAN-DEC MON-FRI")
    private void settleTransactionMonth() {
        //CHECK MERCHANT SETTLEMENT SETTINGS
    }

    @Scheduled(cron = "0 0 0 1 JAN-DEC MON-FRI")
    private void settleEveryFirstDayOfTheMonth() {
        //CHECK MERCHANT SETTLEMENT SETTINGS
    }

    @Scheduled(cron = "0 0 9-17 * * MON-FRI")
    private void settleEveryNineToFiveEveryMondayToFriday() {
        //CHECK MERCHANT SETTLEMENT SETTINGS
    }

    @Scheduled(cron = "0 0 17 * * FRI")
    private void settleEveryFivePMEveryFriday() {
        //CHECK MERCHANT SETTLEMENT SETTINGS
    }

    private void processFundMerchantWalletAccount() {
        new Thread(() -> {
            List<PaymentGateway> payment = paymentGatewayRepo.findAllNotFlaggedAndSuccessful();
            for (PaymentGateway mPayment : payment) {
                try {
                    PaymentGateway sPayment = paymentGatewayRepo.findByRefNo(mPayment.getRefNo()).orElse(null);
                    if (ObjectUtils.isEmpty(sPayment))
                        continue;
                    FundEventResponse response = uniPayProxy.postTransactionPosition(paymentGateWayCommonUtils.getDaemonAuthToken(), mPayment);
                    if (response.getPostedFlg() && (!response.getTranId().isBlank())) {
                        sPayment.setTranflg(true);
                        paymentGatewayRepo.save(sPayment);
                    }
                } catch (Exception ex) {
                    log.error("WALLET TRANSACTION FAILED: " + ex.getLocalizedMessage());
                }
            }
        }).start();
    }
}
