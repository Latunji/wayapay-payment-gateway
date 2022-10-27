package com.wayapaychat.paymentgateway.cron;

import com.wayapaychat.paymentgateway.common.utils.PaymentGateWayCommonUtils;
import com.wayapaychat.paymentgateway.dao.WayaPaymentDAO;
import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.entity.RecurrentTransaction;
import com.wayapaychat.paymentgateway.entity.listener.PaymemtGatewayEntityListener;
import com.wayapaychat.paymentgateway.enumm.TransactionStatus;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaTransactionQuery;
import com.wayapaychat.paymentgateway.pojo.waya.FundEventResponse;
import com.wayapaychat.paymentgateway.proxy.AuthApiClient;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import com.wayapaychat.paymentgateway.repository.RecurrentTransactionRepository;
import com.wayapaychat.paymentgateway.service.PaymentGatewayService;
import com.wayapaychat.paymentgateway.service.impl.UnifiedPaymentProxy;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@EnableAsync
@Slf4j
public class CronService {

    private final ExecutorService executorService = Executors.newWorkStealingPool(10);
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
    @Autowired
    private PaymemtGatewayEntityListener paymemtGatewayEntityListener;
    private WayaPaymentDAO wayaPaymentDAO;


//    @Scheduled(cron = "*/30 * * * * *") // 30sec for dev env
    @Scheduled(cron = "* 20 * * * *") // 20min for staging and prod
    @SchedulerLock(name = "TaskScheduler_updateTransactionStatusEveryDay", lockAtLeastFor = "10s", lockAtMostFor = "30s")
    public void updateTransactionStatusEveryDay() {
        log.info("----------------------------- Starting SCHEDULE -----------------------------");
        updateTransactionStatus();
    }

    private void updateTransactionStatus() {
        log.info("------ ------ ------ ----------- Starting Tranx Updates -----------------------------");
        executorService.submit(() -> {
            List<PaymentGateway> product = paymentGatewayRepo.findAllFailedAndPendingTransactions();
            log.info("------ TRANSACTIONS: "+ product.toString());
            product.parallelStream().forEach(mPay -> {
                log.info("----------######------- LOOPING TRANSACTION UPDATES ----------######-------");
                log.info("----------######------- this tranx status: "+mPay.getStatus().toString()+" ----------######-------");
//                if (mPay.getStatus() != TransactionStatus.SUCCESSFUL && mPay.getStatus() != TransactionStatus.FAILED) {
                if (mPay.getStatus() == TransactionStatus.PENDING) {
                    log.info("----------######------- tranx pending ----------######-------");
                    if (!mPay.getTranId().isBlank() && StringUtils.isNumeric(mPay.getTranId())) {
                        log.info("----------######------- NSNF Tranx not blank --- now processing ----------######-------");
                        WayaTransactionQuery query = paymentService.getTransactionStatus(mPay.getTranId());
                        preprocessTransactionStatus(mPay, query);
                    }
                    else {
                        killSuspectedTransactions(mPay);
                        log.info("----------######------- pending tranx KILLED ----------######-------");
                    }
                } else if (mPay.getStatus() == TransactionStatus.FAILED) {
                    log.info("----------######------- tranx failed ----------######-------");
                    if (!mPay.getTranId().isBlank() && StringUtils.isNumeric(mPay.getTranId())) {
                        log.info("----------######------- F Tranx not blank --- now processing ----------######-------");
                        WayaTransactionQuery query = paymentService.getTransactionStatus(mPay.getTranId());
                        preprocessTransactionStatus(mPay, query);
                    }
                    else {
                        killSuspectedTransactions(mPay);
                        log.info("----------######------- failed tranx KILLED ----------######-------");
                    }
                }
            });
        });
    }

    //TODO: Process if transaction was successful before and then was not successful again,
    // reverse the transaction and then debit the merchant if the merchant has been credited before
    private void preprocessTransactionStatus(PaymentGateway mPay, WayaTransactionQuery query) {
        log.info("----------######------- processing tranx [UNI status = "+query.getStatus()+"] ----------######-------");
        try {
            log.info("----------######------- now trying to apply update ----------######-------");
            if (query.getStatus().contains("APPROVED") && !mPay.getStatus().equals(TransactionStatus.SUCCESSFUL)) {
                mPay.setStatus(TransactionStatus.SUCCESSFUL);
                mPay.setSuccessfailure(true);
                mPay.setSessionId(query.getSessionId());
                mPay.setProcessingFee(new BigDecimal(query.getConvenienceFee()));
                if (mPay.getIsFromRecurrentPayment())
                    paymentService.updateRecurrentTransaction(mPay);
                paymentGatewayRepo.save(mPay);
                log.info("------||| transaction "+mPay.getTranId()+" set to successful |||-------");
                paymemtGatewayEntityListener.sendTransactionNotificationAfterPaymentIsSuccessful(mPay);
            } else if (query.getStatus().contains("REJECT")) {
                mPay.setStatus(TransactionStatus.REJECTED);
                mPay.setSuccessfailure(false);
                paymentGatewayRepo.save(mPay);
                log.info("------||| transaction "+mPay.getTranId()+" set to REJECTED |||-------");
            }
        } catch (Exception e) {
            log.error("------||||SYSTEM ERROR ON PROCESSING TRANSACTION FROM CRON||||-------", e);
            mPay.setStatus(TransactionStatus.FAILED);
            mPay.setSuccessfailure(false);
            paymentGatewayRepo.save(mPay);
            log.info("------||| transaction "+mPay.getTranId()+" set to FAILED |||-------");
        }
    }

    private void killSuspectedTransactions(PaymentGateway mPay) {
        log.info("----------######------- tranx KILLER: "+ mPay.getTranId() +" ----------######-------");
        mPay.setStatus(TransactionStatus.SYSTEM_ERROR);
        mPay.setSuccessfailure(false);
        paymentGatewayRepo.save(mPay);
        log.info("------||| transaction "+mPay.getTranId()+" K.I.L.L.E.D |||-------");
    }

//    @Scheduled(cron = "0 0 0 * * *")
//    public void runEveryDay() {
//        processNextRecurrentTransaction();
//    }

    // s-l done
    @Scheduled(cron = "* */3 * * * *")
    @Async
    @SchedulerLock(name = "TaskScheduler_expireTransactionAfterThirtyMinutes")
    public void expireTransactionAfterThirtyMinutes() {
        log.info("------||| expiring transactions that have stayed more than 30min NOW |||-------");
        wayaPaymentDAO.expireAllTransactionMoreThan30Mins();
        log.info("------||| transactions that have stayed more than 30min have all EXPIRED on live and sandbox |||-------");
    }

//    @Scheduled(cron = "*/10 * * * * *")
//    @SchedulerLock(name = "TaskScheduler_settleEveryFiveSeconds", lockAtLeastFor = "10s", lockAtMostFor = "15s")
//    public void settleEveryFiveSeconds() {
//        log.info("------|--|--| FIVE SECOND SETTLEMENT HAPPENING |--|--|-------");
//        // prorcessThirdPartyPaymentProcessed(); just commented this
//        log.info("------|--|--| FIVE SECOND SETTLEMENT ENDED |--|--|-------");
//    }

    public void prorcessThirdPartyPaymentProcessed() {
        new Thread(() -> {
            List<PaymentGateway> paymentList = paymentGatewayRepo.findAllNotFlaggedAndSuccessful();
            String token = paymentGateWayCommonUtils.getDaemonAuthToken();
            for (PaymentGateway payment : paymentList) {
                try {
                    // this check is not necessary, its distorts efficiency
//                    PaymentGateway sPayment = paymentGatewayRepo.findByRefNo(payment.getRefNo()).orElse(null);
//                    if (ObjectUtils.isEmpty(sPayment))
//                        continue;
                    FundEventResponse response = uniPayProxy.postTransactionPosition(token, payment);
                    if (response.getPostedFlg() && (!response.getTranId().isBlank())) {
                        payment.setTranflg(true);
                        paymentGatewayRepo.save(payment);
                        log.error("TRANSACTION FLAGED FOR: " + payment.getRefNo());
                    }
                } catch (Exception ex) {
                    log.error("WALLET TRANSACTION FAILED: " + ex.getLocalizedMessage());
                }
            }
        }).start();
    }

    private void processNextRecurrentTransaction() {
        new Thread(() -> {
            List<RecurrentTransaction> recurrentTransactions = recurrentTransactionRepository.findNextRecurrentTransaction();
        }).start();
    }
}
