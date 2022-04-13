package com.wayapaychat.paymentgateway.cron;

import com.wayapaychat.paymentgateway.common.utils.PaymentGateWayCommonUtils;
import com.wayapaychat.paymentgateway.dao.WayaPaymentDAO;
import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.entity.TransactionSettlement;
import com.wayapaychat.paymentgateway.enumm.AccountSettlementOption;
import com.wayapaychat.paymentgateway.enumm.SettlementStatus;
import com.wayapaychat.paymentgateway.pojo.waya.FundEventResponse;
import com.wayapaychat.paymentgateway.pojo.waya.MerchantUnsettledSuccessfulTransaction;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantData;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantResponse;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.WalletResponse;
import com.wayapaychat.paymentgateway.proxy.AuthApiClient;
import com.wayapaychat.paymentgateway.proxy.IdentityManager;
import com.wayapaychat.paymentgateway.proxy.WalletProxy;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import com.wayapaychat.paymentgateway.repository.RecurrentTransactionRepository;
import com.wayapaychat.paymentgateway.repository.TransactionSettlementRepository;
import com.wayapaychat.paymentgateway.service.PaymentGatewayService;
import com.wayapaychat.paymentgateway.service.impl.UnifiedPaymentProxy;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionSettlementCronService {

    @Autowired
    public RecurrentTransactionRepository recurrentTransactionRepository;
    @Autowired
    public TransactionSettlementRepository transactionSettlementRepository;
    @Autowired
    public WayaPaymentDAO wayaPaymentDAO;
    @Value("${service.name}")
    public String username;
    @Value("${service.pass}")
    public String passSecret;
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
    private IdentityManager identityManager;
    @Autowired
    private WalletProxy walletProxy;


    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "TaskScheduler_createAndUpdateMerchantTransactionSettlement", lockAtLeastFor = "10s", lockAtMostFor = "15s")
    public void createAndUpdateMerchantTransactionSettlement() {
        LockAssert.assertLocked();
        CompletableFuture.runAsync(() -> {
            List<MerchantUnsettledSuccessfulTransaction> merchantUnsettledSuccessfulTransactions = wayaPaymentDAO.merchantUnsettledSuccessTransactions(null);
            List<TransactionSettlement> pendingMerchantSettlements = transactionSettlementRepository.findAllMerchantSettlementPending();

            Map<String, TransactionSettlement> merchantWithPendingUnsettledTransaction = pendingMerchantSettlements
                    .parallelStream()
                    .collect(Collectors.toMap(TransactionSettlement::getMerchantId, Function.identity(), (o1, o2) -> o1));
            Set<String> merchantsWithPendingUnsettledTransactions = merchantWithPendingUnsettledTransaction.keySet();

            merchantUnsettledSuccessfulTransactions.parallelStream().forEach(unsettledSuccessfulTransaction -> {
                String merchantId = unsettledSuccessfulTransaction.getMerchantId();
                if (merchantsWithPendingUnsettledTransactions.contains(merchantId)) {
                    TransactionSettlement transactionSettlement = merchantWithPendingUnsettledTransaction.get(merchantId);
                    transactionSettlement.setDateModified(LocalDateTime.now());
                    transactionSettlement.setModifiedBy(0L);
                    transactionSettlement.setSettlementStatus(SettlementStatus.PENDING);
                    transactionSettlement.setTotalFee(unsettledSuccessfulTransaction.getTotalFee());
                    transactionSettlement.setSettlementNetAmount(unsettledSuccessfulTransaction.getNetAmount());
                    transactionSettlement.setSettlementGrossAmount(unsettledSuccessfulTransaction.getGrossAmount());
                    transactionSettlement = transactionSettlementRepository.save(transactionSettlement);
                    //process the merchant settlement from here
                    //TODO process the merchant account settlement
                    //processExpiredMerchantConfiguredSettlement(transactionSettlement);
                } else {
                    TransactionSettlement transactionSettlement = TransactionSettlement.builder()
                            .settlementGrossAmount(unsettledSuccessfulTransaction.getGrossAmount())
                            .settlementNetAmount(unsettledSuccessfulTransaction.getNetAmount())
                            .totalFee(unsettledSuccessfulTransaction.getTotalFee())
                            .settlementStatus(SettlementStatus.PENDING)
                            .accountSettlementOption(AccountSettlementOption.WALLET)
                            .merchantId(merchantId)
                            .build();
                    transactionSettlement.setCreatedBy(0L);
                    transactionSettlement.setDateCreated(LocalDateTime.now());
                    transactionSettlement.setDeleted(false);
                    //TODO: SETTINGS
                    // Get the merchant default settings for settling the account
                    // for now use the merchant wallet to deposit to the account
                    transactionSettlementRepository.save(transactionSettlement);
                }
            });
        });
    }

    private void processExpiredMerchantConfiguredSettlement(TransactionSettlement transactionSettlement) {
        new Thread(() -> {
            try {
                LocalDateTime merchantConfiguredSettlementDate = transactionSettlement.getMerchantConfiguredSettlementDate();
                if (merchantConfiguredSettlementDate.isAfter(LocalDateTime.now()) || merchantConfiguredSettlementDate.isEqual(LocalDateTime.now())) {
                    log.info("--------||||PROCESSING MERCHANT TRANSACTION SETTLEMENT||||----------");
                    LocalDateTime settlementInitiatedAt = LocalDateTime.now();
                    BigDecimal amountToSettle = transactionSettlement.getSettlementNetAmount();
                    AccountSettlementOption accountSettlementOption = transactionSettlement.getAccountSettlementOption();
                    String merchantId = transactionSettlement.getMerchantId();
                    if (transactionSettlement.getAccountSettlementOption().equals(AccountSettlementOption.BANK)) {
                        //TODO: Get the merchant settings and the bank account used for settlement
                        // Call the withdrawal endpoint to make payment to the user settlement account
                    } else if (transactionSettlement.getAccountSettlementOption().equals(AccountSettlementOption.WALLET)) {
                        @NotNull final String daemonToken = paymentGateWayCommonUtils.getDaemonAuthToken();
                        MerchantResponse merchantResponse = identityManager.getMerchantDetail(daemonToken, merchantId);
                        MerchantData merchantData = merchantResponse.getData();
                        if (ObjectUtils.isNotEmpty(merchantData)) {
                            WalletResponse wallet = walletProxy.getUserDefaultWalletAccount(paymentGateWayCommonUtils.getDaemonAuthToken(), merchantData.getUserId());
                            if (!wallet.getStatus()) {
                                log.error("WALLET ERROR: " + wallet);
                                transactionSettlement.setSettlementStatus(SettlementStatus.FAILED);
                                transactionSettlementRepository.save(transactionSettlement);
                                log.info("--------||||FAILED PROCESSING MERCHANT SETTLEMENT||||----------");
                            } else {
                                //TODO: PROCESS PAYMENT TO THE USER ACCOUNT
                            }
                        }
                        //TODO: process the wallet payment to merchant default wallet account
                        // Call the waya-merchant/settlement-accounts/default-account
                    }
                    LocalDateTime dateSettled = LocalDateTime.now();
                    transactionSettlement.setDateSettled(dateSettled);
                    transactionSettlement.setSettlementInitiationDate(settlementInitiatedAt);
                    transactionSettlementRepository.save(transactionSettlement);
                    log.info("--------||||COMPLETED PROCESSING MERCHANT SETTLEMENT TO {} ACCOUNT ||||----------", transactionSettlement.getAccountSettlementOption());
                }
            } catch (Exception e) {
                transactionSettlement.setSettlementStatus(SettlementStatus.FAILED);
                transactionSettlementRepository.save(transactionSettlement);
                log.info("--------||||FAILED PROCESSING MERCHANT SETTLEMENT||||----------");
            }
        }).start();
    }

    @Scheduled(cron = "*/5 * * * * *")
    @SchedulerLock(name = "TaskScheduler_settleEveryFiveSeconds", lockAtLeastFor = "10s", lockAtMostFor = "15s")
    public void settleEveryFiveSeconds() {
//        processFundMerchantWalletAccount();
    }

    @Scheduled(cron = "0 0 0 28-31 JAN-DEC MON-FRI")
    public void settleTransactionMonth() {
        //CHECK MERCHANT SETTLEMENT SETTINGS
    }

    @Scheduled(cron = "0 0 0 1 JAN-DEC MON-FRI")
    public void settleEveryFirstDayOfTheMonth() {
        //CHECK MERCHANT SETTLEMENT SETTINGS
    }

    @Scheduled(cron = "0 0 9-17 * * MON-FRI")
    public void settleEveryNineToFiveEveryMondayToFriday() {
        //CHECK MERCHANT SETTLEMENT SETTINGS
    }

    @Scheduled(cron = "0 0 17 * * FRI")
    public void settleEveryFivePMEveryFriday() {
        //CHECK MERCHANT SETTLEMENT SETTINGS
    }

    public void processFundMerchantWalletAccount() {
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
