package com.wayapaychat.paymentgateway.cron;

import com.wayapaychat.paymentgateway.common.utils.PaymentGateWayCommonUtils;
import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.entity.RecurrentTransaction;
import com.wayapaychat.paymentgateway.entity.listener.PaymemtGatewayEntityListener;
import com.wayapaychat.paymentgateway.enumm.TransactionStatus;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaTransactionQuery;
import com.wayapaychat.paymentgateway.proxy.AuthApiClient;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import com.wayapaychat.paymentgateway.repository.RecurrentTransactionRepository;
import com.wayapaychat.paymentgateway.service.PaymentGatewayService;
import com.wayapaychat.paymentgateway.service.impl.UnifiedPaymentProxy;
import lombok.extern.slf4j.Slf4j;
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


    @Scheduled(cron = "0 0 0 * * *")
    private void runEveryFiveSeconds() {
        updateTransactionStatus();
    }

    @Scheduled(cron = "0 0 0 * * *")
    private void runEveryDay() {
//        processNextRecurrentTransaction();
    }

    private void updateTransactionStatus() {
        executorService.submit(() -> {
            List<PaymentGateway> product = paymentGatewayRepo.findAllFailedAndPendingTransactions();
            product.parallelStream().forEach(mPay -> {
                if (mPay != null) {
                    if (mPay.getStatus() != TransactionStatus.SUCCESSFUL && mPay.getStatus() != TransactionStatus.FAILED) {
                        if (!mPay.getTranId().isBlank() && StringUtils.isNumeric(mPay.getTranId())) {
                            WayaTransactionQuery query = paymentService.getTransactionStatus(mPay.getTranId());
                            preprocessSuccessfulTransaction(mPay, query);
                        }
                    } else if (mPay.getStatus() == TransactionStatus.FAILED) {
                        if (!mPay.getTranId().isBlank() && StringUtils.isNumeric(mPay.getTranId())) {
                            WayaTransactionQuery query = paymentService.getTransactionStatus(mPay.getTranId());
                            preprocessSuccessfulTransaction(mPay, query);
                        }
                    }
                }
            });
        });
    }

    //TODO: Process if transaction was successful before and then was not successful again,
    // reverse the transaction and then debit the merchant if the merchant has been credited before
    private void preprocessSuccessfulTransaction(PaymentGateway mPay, WayaTransactionQuery query) {
        try {
            if (query.getStatus().contains("APPROVED") && !mPay.getStatus().equals(TransactionStatus.SUCCESSFUL)) {
                mPay.setStatus(TransactionStatus.SUCCESSFUL);
                mPay.setSuccessfailure(true);
                mPay.setSessionId(query.getSessionId());
                mPay.setProcessingFee(new BigDecimal(query.getConvenienceFee()));
                if (mPay.getIsFromRecurrentPayment())
                    paymentService.updateRecurrentTransaction(mPay);
                paymentGatewayRepo.save(mPay);
                paymemtGatewayEntityListener.sendTransactionNotificationAfterPaymentIsSuccessful(mPay);
            } else if (query.getStatus().contains("REJECT")) {
                mPay.setStatus(TransactionStatus.FAILED);
                mPay.setSuccessfailure(false);
                paymentGatewayRepo.save(mPay);
            }
        } catch (Exception e) {
            log.error("------||||SYSTEM ERROR||||-------", e);
            mPay.setStatus(TransactionStatus.FAILED);
            mPay.setSuccessfailure(false);
            paymentGatewayRepo.save(mPay);
        }
    }

    private void processNextRecurrentTransaction() {
        new Thread(() -> {
            List<RecurrentTransaction> recurrentTransactions = recurrentTransactionRepository.findNextRecurrentTransaction();
        }).start();
    }
}
