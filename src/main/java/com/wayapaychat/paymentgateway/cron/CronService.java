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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
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
    @Scheduled(cron = "* */20 * * * *") // 20min for staging and prod
    @SchedulerLock(name = "TaskScheduler_updateTransactionStatusEveryDay", lockAtLeastFor = "10s", lockAtMostFor = "30s")
    public void updateTransactionStatusEveryDay() {
        log.info("----------------------------- Starting SCHEDULE -----------------------------");
        updateTransactionStatus();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void runEveryDay() {
//        processNextRecurrentTransaction();
    }

    private void updateTransactionStatus() {
        log.info("----------------------------- Starting Tranx Updates -----------------------------");
        executorService.submit(() -> {
            List<PaymentGateway> product = paymentGatewayRepo.findAllFailedAndPendingTransactions();
            log.info("------ TRANSACTIONS: "+ product.toString());
            product.parallelStream().forEach(mPay -> {
                log.info("----------######------- LOOPING ----------######-------");
                if (mPay.getStatus() != TransactionStatus.SUCCESSFUL && mPay.getStatus() != TransactionStatus.FAILED) {
                    log.info("----------######------- tranx not successful and not failed ----------######-------");
                    if (!mPay.getTranId().isBlank() && StringUtils.isNumeric(mPay.getTranId())) {
                        WayaTransactionQuery query = paymentService.getTransactionStatus(mPay.getTranId());
                        preprocessSuccessfulTransaction(mPay, query);
                    }
                } else if (mPay.getStatus() == TransactionStatus.FAILED) {
                    log.info("----------######------- tranx failed ----------######-------");
                    if (!mPay.getTranId().isBlank() && StringUtils.isNumeric(mPay.getTranId())) {
                        WayaTransactionQuery query = paymentService.getTransactionStatus(mPay.getTranId());
                        preprocessSuccessfulTransaction(mPay, query);
                    }
                }
            });
        });
    }

    //TODO: Process if transaction was successful before and then was not successful again,
    // reverse the transaction and then debit the merchant if the merchant has been credited before
    private void preprocessSuccessfulTransaction(PaymentGateway mPay, WayaTransactionQuery query) {
        log.info("----------######------- processing tranx ----------######-------");
        log.info("----------######------- tranx ext status on: "+query.getStatus()+" ----------######-------");
        try {
            log.info("----------######------- trying to apply update ----------######-------");
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
                mPay.setStatus(TransactionStatus.FAILED);
                mPay.setSuccessfailure(false);
                paymentGatewayRepo.save(mPay);
                log.info("------||| transaction "+mPay.getTranId()+" set to FAILED |||-------");
            }
        } catch (Exception e) {
            log.error("------||||SYSTEM ERROR ON PROCESSING TRANSACTION FROM CRON||||-------", e);
            mPay.setStatus(TransactionStatus.FAILED);
            mPay.setSuccessfailure(false);
            paymentGatewayRepo.save(mPay);
            log.info("------||| transaction "+mPay.getTranId()+" set to FAILED |||-------");
        }
    }

    @Scheduled(cron = "* */30 * * * *")
    @SchedulerLock(name = "TaskScheduler_expireTransactionAfterThirtyMinutes", lockAtLeastFor = "10s", lockAtMostFor = "30s")
    public void expireTransactionAfterThirtyMinutes() {
        wayaPaymentDAO.expireAllTransactionLessThan30Mins();
    }

    @Scheduled(cron = "* */30 * * * *")
    @SchedulerLock(name = "TaskScheduler_settleEveryFiveSeconds", lockAtLeastFor = "10s", lockAtMostFor = "15s")
    public void settleEveryFiveSeconds() {
        prorcessThirdPartyPaymentProcessed();
    }

    public void prorcessThirdPartyPaymentProcessed() {
        new Thread(() -> {
            List<PaymentGateway> payment = paymentGatewayRepo.findAllNotFlaggedAndSuccessful();
            String token = paymentGateWayCommonUtils.getDaemonAuthToken();
            for (PaymentGateway mPayment : payment) {
                try {
                    PaymentGateway sPayment = paymentGatewayRepo.findByRefNo(mPayment.getRefNo()).orElse(null);
                    if (ObjectUtils.isEmpty(sPayment))
                        continue;
                    FundEventResponse response = uniPayProxy.postTransactionPosition(token, mPayment);
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

    private void processNextRecurrentTransaction() {
        new Thread(() -> {
            List<RecurrentTransaction> recurrentTransactions = recurrentTransactionRepository.findNextRecurrentTransaction();
        }).start();
    }
}
