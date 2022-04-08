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

import java.util.List;

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

    @Value("${service.pass}")
    private String passSecret;


    @Scheduled(cron = "*/5 * * * * *")
    private void runEveryFiveSeconds() {
//        updateTransactionStatus();
    }

    @Scheduled(cron = "0 0 0 * * *")
    private void runEveryDay() {
//        processNextRecurrentTransaction();
    }

    private void updateTransactionStatus() {
        new Thread(() -> {
            List<PaymentGateway> product = paymentGatewayRepo.findAll();
            for (PaymentGateway payment : product) {
                PaymentGateway mPay = paymentGatewayRepo.findByRefNo(payment.getRefNo()).orElse(null);
                if (mPay != null) {
                    if (mPay.getStatus() != null) {
                        if ((mPay.getStatus().compareTo(TransactionStatus.TRANSACTION_COMPLETED) != 0)
                                && (mPay.getStatus().compareTo(TransactionStatus.SUCCESSFUL) != 0)
                                && (mPay.getStatus().compareTo(TransactionStatus.FAILED) != 0)) {
                            if (!mPay.getTranId().isBlank() && StringUtils.isNumeric(mPay.getTranId())) {
                                WayaTransactionQuery query = paymentService.getTransactionStatus(mPay.getTranId());
                                if (query.getStatus().contains("APPROVED")) {
                                    mPay.setStatus(TransactionStatus.TRANSACTION_COMPLETED);
                                    mPay.setSuccessfailure(true);
                                    mPay.setSessionId(query.getSessionId());
                                    if (mPay.getIsFromRecurrentPayment()) {
                                        paymentService.updateRecurrentTransaction(mPay);
                                    }
                                } else if (query.getStatus().contains("REJECT")) {
                                    mPay.setStatus(TransactionStatus.TRANSACTION_FAILED);
                                    mPay.setSuccessfailure(false);
                                }
                                paymentGatewayRepo.save(mPay);
                            }

                        } else if (mPay.getStatus().compareTo(TransactionStatus.TRANSACTION_COMPLETED) == 0) {
                            if (!mPay.getTranId().isBlank() && StringUtils.isNumeric(mPay.getTranId())) {
                                WayaTransactionQuery query = paymentService.getTransactionStatus(mPay.getTranId());
                                mPay.setStatus(TransactionStatus.SUCCESSFUL);
                                mPay.setSuccessfailure(true);
                                if (query.getStatus().contains("APPROVED")) {
                                    mPay.setStatus(TransactionStatus.SUCCESSFUL);
                                    mPay.setSuccessfailure(true);
                                } else if (query.getStatus().contains("REJECT")) {
                                    mPay.setStatus(TransactionStatus.FAILED);
                                    mPay.setSuccessfailure(false);
                                }
                                paymentGatewayRepo.save(mPay);
                            }
                        } else if (mPay.getStatus().compareTo(TransactionStatus.TRANSACTION_FAILED) == 0) {
                            if (!mPay.getTranId().isBlank() && StringUtils.isNumeric(mPay.getTranId())) {
                                WayaTransactionQuery query = paymentService.getTransactionStatus(mPay.getTranId());
                                mPay.setStatus(TransactionStatus.FAILED);
                                mPay.setSuccessfailure(false);
                                if (query.getStatus().contains("APPROVED")) {
                                    mPay.setStatus(TransactionStatus.SUCCESSFUL);
                                    mPay.setSuccessfailure(true);
                                } else if (query.getStatus().contains("REJECT")) {
                                    mPay.setStatus(TransactionStatus.FAILED);
                                    mPay.setSuccessfailure(false);
                                }
                                paymentGatewayRepo.save(mPay);
                            }
                        } else if (mPay.getStatus().compareTo(TransactionStatus.TRANSACTION_PENDING) == 0) {
                            if (!mPay.getTranId().isBlank() && StringUtils.isNumeric(mPay.getTranId())) {
                                WayaTransactionQuery query = paymentService.getTransactionStatus(mPay.getTranId());
                                mPay.setStatus(TransactionStatus.PENDING);
                                mPay.setSuccessfailure(false);
                                if (query.getStatus().contains("APPROVED")) {
                                    mPay.setStatus(TransactionStatus.SUCCESSFUL);
                                    mPay.setSuccessfailure(true);
                                } else if (query.getStatus().contains("REJECT")) {
                                    mPay.setStatus(TransactionStatus.FAILED);
                                    mPay.setSuccessfailure(false);
                                }
                                paymentGatewayRepo.save(mPay);
                            }
                        }
                    }
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
