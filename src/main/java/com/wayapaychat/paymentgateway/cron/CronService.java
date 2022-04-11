package com.wayapaychat.paymentgateway.cron;

import com.wayapaychat.paymentgateway.common.utils.PaymentGateWayCommonUtils;
import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.entity.RecurrentTransaction;
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
    private final ExecutorService executorService = Executors.newWorkStealingPool(10);

    @Value("${service.pass}")
    private String passSecret;


    @Scheduled(cron = "*/5 * * * * *")
    private void runEveryFiveSeconds() {
        updateTransactionStatus();
    }

    @Scheduled(cron = "0 0 0 * * *")
    private void runEveryDay() {
//        processNextRecurrentTransaction();
    }

    private void updateTransactionStatus() {
        executorService.submit(() -> {
            List<PaymentGateway> product = paymentGatewayRepo.findAll();
            product.parallelStream().forEach(payment -> {
                PaymentGateway mPay = paymentGatewayRepo.findByRefNo(payment.getRefNo()).orElse(null);
                if (mPay != null) {
                    if (mPay.getStatus() != null) {
                        if (mPay.getStatus() != TransactionStatus.SUCCESSFUL && mPay.getStatus() != TransactionStatus.FAILED) {
                            if (!mPay.getTranId().isBlank() && StringUtils.isNumeric(mPay.getTranId())) {
                                WayaTransactionQuery query = paymentService.getTransactionStatus(mPay.getTranId());
                                preprocessSuccessfulTransaction(mPay, query);
                                paymentGatewayRepo.save(mPay);
                            }
                        } else if (mPay.getStatus() == TransactionStatus.SUCCESSFUL) {
                            if (!mPay.getTranId().isBlank() && StringUtils.isNumeric(mPay.getTranId())) {
                                WayaTransactionQuery query = paymentService.getTransactionStatus(mPay.getTranId());
                                preprocessSuccessfulTransaction(mPay, query);
                                paymentGatewayRepo.save(mPay);
                            }
                        } else if (mPay.getStatus() == TransactionStatus.FAILED) {
                            if (!mPay.getTranId().isBlank() && StringUtils.isNumeric(mPay.getTranId())) {
                                WayaTransactionQuery query = paymentService.getTransactionStatus(mPay.getTranId());
                                preprocessSuccessfulTransaction(mPay, query);
                                paymentGatewayRepo.save(mPay);
                            }
                        }
                    }
                }
            });
        });
    }

    //TODO: Process if transaction was successful before and then was not successful again,
    // reverse the transaction and then debit the merchant if the merchant has been credited before
    private void preprocessSuccessfulTransaction(PaymentGateway mPay, WayaTransactionQuery query) {
        try{
            if (query.getStatus().contains("APPROVED")) {
                mPay.setStatus(TransactionStatus.SUCCESSFUL);
                mPay.setSuccessfailure(true);
                mPay.setSessionId(query.getSessionId());
                mPay.setProcessingFee(new BigDecimal(query.getConvenienceFee()));
                if (mPay.getIsFromRecurrentPayment())
                    paymentService.updateRecurrentTransaction(mPay);
            } else if (query.getStatus().contains("REJECT")) {
                mPay.setStatus(TransactionStatus.FAILED);
                mPay.setSuccessfailure(false);
            }
        }catch (Exception e){
            log.error("------||||SYSTEM ERROR||||-------",e);
            mPay.setStatus(TransactionStatus.SYSTEM_ERROR);
            paymentGatewayRepo.save(mPay);
        }
    }

    private void processNextRecurrentTransaction() {
        new Thread(() -> {
            List<RecurrentTransaction> recurrentTransactions = recurrentTransactionRepository.findNextRecurrentTransaction();
        }).start();
    }
}
