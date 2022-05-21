package com.wayapaychat.paymentgateway.cron;

import com.wayapaychat.paymentgateway.common.enums.Interval;
import com.wayapaychat.paymentgateway.common.utils.PaymentGateWayCommonUtils;
import com.wayapaychat.paymentgateway.dao.WayaPaymentDAO;
import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.entity.TransactionSettlement;
import com.wayapaychat.paymentgateway.enumm.AccountSettlementOption;
import com.wayapaychat.paymentgateway.enumm.EventType;
import com.wayapaychat.paymentgateway.enumm.SettlementStatus;
import com.wayapaychat.paymentgateway.kafkamessagebroker.model.ProducerMessageDto;
import com.wayapaychat.paymentgateway.kafkamessagebroker.producer.IkafkaMessageProducer;
import com.wayapaychat.paymentgateway.pojo.waya.FundEventResponse;
import com.wayapaychat.paymentgateway.pojo.waya.MerchantUnsettledSuccessfulTransaction;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantData;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantResponse;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.DefaultWalletResponse;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.WalletSettlementResponse;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.WalletSettlementWithEventIdPojo;
import com.wayapaychat.paymentgateway.proxy.AuthApiClient;
import com.wayapaychat.paymentgateway.proxy.IdentityManagementServiceProxy;
import com.wayapaychat.paymentgateway.proxy.WalletProxy;
import com.wayapaychat.paymentgateway.proxy.pojo.WayaMerchantConfiguration;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import com.wayapaychat.paymentgateway.repository.RecurrentTransactionRepository;
import com.wayapaychat.paymentgateway.repository.TransactionSettlementRepository;
import com.wayapaychat.paymentgateway.service.PaymentGatewayService;
import com.wayapaychat.paymentgateway.service.impl.UnifiedPaymentProxy;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
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
    @Value("${service.token}")
    public String daemonToken;
    @Value("${waya.wallet.wayapay-debit-account}")
    public String settlementWallet;
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
    private IdentityManagementServiceProxy identityManagementServiceProxy;
    @Autowired
    private WalletProxy walletProxy;
    @Value(value = "${waya.wallet.wayapay-debit-account}")
    private String debitWalletEventId;
    @Autowired
    private IkafkaMessageProducer ikafkaMessageProducer;

    @Scheduled(cron = "*/59 * * * * *")
    @SchedulerLock(name = "TaskScheduler_createAndUpdateMerchantTransactionSettlement", lockAtLeastFor = "10s", lockAtMostFor = "30s")
    public void createAndUpdateMerchantTransactionSettlement() {
        LockAssert.assertLocked();
        List<MerchantUnsettledSuccessfulTransaction> merchantUnsettledSuccessfulTransactions = wayaPaymentDAO.merchantUnsettledSuccessTransactions(null);
        List<TransactionSettlement> pendingMerchantSettlements = transactionSettlementRepository.findAllMerchantSettlementPending();

        Map<String, TransactionSettlement> merchantWithPendingUnsettledTransaction = pendingMerchantSettlements.parallelStream()
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
                log.info("--------||||SUCCESSFULLY UPDATED NEXT MERCHANT TRANSACTION SETTLEMENT||||----------");
                transactionSettlementRepository.save(transactionSettlement);
            } else {
                List<PaymentGateway> merchantUnsettledPayments = paymentGatewayRepo.getAllTransactionNotSettled(merchantId);
                //TODO: Get the merchant configuration
                WayaMerchantConfiguration wayaMerchantConfiguration = null;
                try {
                    wayaMerchantConfiguration = identityManagementServiceProxy.getMerchantConfiguration(merchantId, daemonToken).getData();
                } catch (Exception e) {
                    log.error("--------||||ERROR OCCURRED TO CREATE MERCHANT SETTLEMENT||||----------", e);
                    return;
                }
                @NotNull final String settlementReferenceId = "SET_REF_" + RandomStringUtils.randomAlphanumeric(5) + System.currentTimeMillis()
                        + RandomStringUtils.randomAlphanumeric(5);
                Interval settlementInterval = ObjectUtils.isEmpty(wayaMerchantConfiguration.getSettlementInterval()) ? Interval.ZERO_DAYS : wayaMerchantConfiguration.getSettlementInterval();
                merchantUnsettledPayments.parallelStream().forEach(paymentGateway -> paymentGateway.setSettlementReferenceId(settlementReferenceId));
                TransactionSettlement transactionSettlement = TransactionSettlement.builder()
                        .settlementGrossAmount(unsettledSuccessfulTransaction.getGrossAmount())
                        .settlementNetAmount(unsettledSuccessfulTransaction.getNetAmount())
                        .totalFee(unsettledSuccessfulTransaction.getTotalFee())
                        .settlementStatus(SettlementStatus.PENDING)
                        .accountSettlementOption(AccountSettlementOption.WALLET)
                        .settlementReferenceId(settlementReferenceId)
                        .merchantConfiguredSettlementDate(LocalDateTime.now().plusDays(settlementInterval.getDays()))
                        .merchantId(merchantId)
                        .build();
                transactionSettlement.setCreatedBy(0L);
                transactionSettlement.setDateCreated(LocalDateTime.now());
                transactionSettlement.setDeleted(false);
                transactionSettlementRepository.save(transactionSettlement);
                paymentGatewayRepo.saveAllAndFlush(merchantUnsettledPayments);
                log.info("--------||||SUCCESSFULLY CREATED MERCHANT TRANSACTION SETTLEMENT||||----------");
            }
        });
    }

    @Scheduled(cron = "*/59 * * * * *")
    @SchedulerLock(name = "TaskScheduler_processSettlementForAllPendingTransactionsMinutes", lockAtLeastFor = "10s", lockAtMostFor = "30s")
    public void processSettlementForAllPendingTransactionsMinutes() {
        processSettlements();
    }

    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "TaskScheduler_processSettlementForAllPendingTransactionsEveryDay", lockAtLeastFor = "10s", lockAtMostFor = "30s")
    public void processSettlementForAllPendingTransactionsEveryDay() {
        processSettlements();
    }

    private void processSettlements() {
        List<TransactionSettlement> allPendingSettlement = transactionSettlementRepository.findAllMerchantSettlementPending();
        allPendingSettlement.parallelStream().forEach(this::processExpiredMerchantConfiguredSettlement);
    }

    private void processExpiredMerchantConfiguredSettlement(TransactionSettlement transactionSettlement) {
        @NotNull final String daemonToken = paymentGateWayCommonUtils.getDaemonAuthToken();
        new Thread(() -> {
            List<PaymentGateway> transactionsToSettle = paymentGatewayRepo.findAllNotSettled(transactionSettlement.getMerchantId());
            try {
                LocalDateTime merchantConfiguredSettlementDate = transactionSettlement.getMerchantConfiguredSettlementDate();
                if (merchantConfiguredSettlementDate.isBefore(LocalDateTime.now()) || merchantConfiguredSettlementDate.isEqual(LocalDateTime.now())) {
                    log.info("--------||||PROCESSING MERCHANT TRANSACTION SETTLEMENT||||----------");
                    LocalDateTime settlementInitiatedAt = LocalDateTime.now();
                    String merchantId = transactionSettlement.getMerchantId();
                    //TODO: calculate the amount to settle from here

                    BigDecimal grossAmount = transactionsToSettle.stream()
                            .map(PaymentGateway::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalFees = transactionsToSettle.stream()
                            .map(PaymentGateway::getFee)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal netAmount = grossAmount.subtract(totalFees);

                    MerchantResponse merchantResponse = identityManagementServiceProxy.getMerchantDetail(daemonToken, merchantId);
                    MerchantData merchantData = merchantResponse.getData();

                    if (transactionSettlement.getAccountSettlementOption().equals(AccountSettlementOption.BANK)) {
                        LocalDateTime dateSettled = LocalDateTime.now();
                        //TODO: Get the merchant settings and the bank account used for settlement
                        // Call the withdrawal endpoint to make payment to the user settlement account
                        saveProcessSettledTransactions(transactionsToSettle, dateSettled, transactionSettlement.getSettlementReferenceId());
                        preprocessSuccessfulSettlement(transactionSettlement, dateSettled, totalFees, netAmount, grossAmount, settlementInitiatedAt, merchantData.getUserId());
                    } else if (transactionSettlement.getAccountSettlementOption().equals(AccountSettlementOption.WALLET)) {

                        if (ObjectUtils.isNotEmpty(merchantData)) {
                            DefaultWalletResponse merchantDefaultWallet = walletProxy.getUserDefaultWalletAccount(daemonToken, merchantData.getUserId());
                            if (!merchantDefaultWallet.getStatus()) {
                                log.error("---------||||COULD NOT FETCH DEFAULT WALLET||||--------------: " + merchantDefaultWallet);
                                preprocessFailedSettlement(transactionSettlement, transactionsToSettle);
                            } else {
                                LocalDateTime dateSettled = LocalDateTime.now();
//                                @NotNull final String DEBIT_WALLET_EVENT_ID = ObjectUtils.isEmpty(debitWalletEventId) ? "WPSETTLE" : debitWalletEventId;
//                                @NotNull final String DEBIT_WALLET_EVENT_ID = Constants.SETTLEMENT_DEBIT_WALLET;
                                @NotNull final String CREDIT_WALLET_ACCOUNT_NO = merchantDefaultWallet.getData().getAccountNo();
                                WalletSettlementWithEventIdPojo walletCreditingRequest = WalletSettlementWithEventIdPojo
                                        .builder()
                                        .amount(netAmount)
                                        .customerAccountNumber(CREDIT_WALLET_ACCOUNT_NO)
                                        .tranCrncy("NGN")
                                        .eventId(settlementWallet)
                                        .tranNarration(String.format("Settlement transaction for %s successful payment", transactionsToSettle.size())
                                        ).transactionCategory("TRANSFER")
                                        .paymentReference(transactionSettlement.getSettlementReferenceId() + "_" + System.currentTimeMillis())
                                        .build();
                                WalletSettlementResponse walletSettlementResponse = walletProxy.creditMerchantDefaultWalletWithEventId(
                                        daemonToken,
                                        walletCreditingRequest
                                );
                                log.info("-----||||WALLET SETTLEMENT RESPONSE FROM TEMPORAL SERVICE|||| {}----", walletSettlementResponse);
                                if (ObjectUtils.isNotEmpty(walletSettlementResponse.getData())) {
                                    log.info("----||||WALLET SETTLEMENT CREDITING TRANSACTION WAS SUCCESSFUL FROM[{}]" +
                                            " TO MERCHANT ACCOUNT NO[{}]||||----", settlementWallet, merchantDefaultWallet.getData().getAccountNo());
                                    saveProcessSettledTransactions(transactionsToSettle, dateSettled, transactionSettlement.getSettlementReferenceId());
                                    preprocessSuccessfulSettlement(transactionSettlement, dateSettled, totalFees, netAmount, grossAmount, settlementInitiatedAt, merchantData.getUserId());
                                } else {
                                    preprocessFailedSettlement(transactionSettlement, transactionsToSettle);
                                }
                                CompletableFuture.runAsync(() -> {
                                    walletCreditingRequest.setMerchantId(transactionSettlement.getMerchantId());
                                    ProducerMessageDto producerMessageDto = ProducerMessageDto.builder()
                                            .data(walletCreditingRequest)
                                            .eventCategory(EventType.MERCHANT_SETTLEMENT_COMPLETED)
                                            .build();
                                    ikafkaMessageProducer.send("merchant", producerMessageDto);
                                });
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.info("------||||ERROR||||------", e);
                preprocessFailedSettlement(transactionSettlement, transactionsToSettle);
            }
        }).start();
    }

    private void preprocessFailedSettlement(TransactionSettlement transactionSettlement, List<PaymentGateway> transactionsToSettle) {
        final Long RETRY_THRESHOLD = 1000000000000000000L;
        if (transactionSettlement.getTotalRetrySettlementCount() < RETRY_THRESHOLD) {
            transactionSettlement.setTotalRetrySettlementCount(transactionSettlement.getTotalRetrySettlementCount() + 1);
            transactionSettlement.setSettlementStatus(SettlementStatus.PENDING);
            transactionsToSettle.parallelStream().forEach(paymentGateway -> {
                paymentGateway.setSettlementStatus(SettlementStatus.PENDING);
                paymentGateway.setSettlementReferenceId(transactionSettlement.getSettlementReferenceId());
            });
            log.info("--------||||FAILED PROCESSING MERCHANT SETTLEMENT... RESET TO PENDING||||----------");
        } else {
            transactionSettlement.setSettlementStatus(SettlementStatus.FAILED);
            transactionsToSettle.parallelStream().forEach(paymentGateway -> paymentGateway.setSettlementStatus(SettlementStatus.FAILED));
            paymentGatewayRepo.saveAllAndFlush(transactionsToSettle);
            log.info("--------||||FAILED PROCESSING MERCHANT SETTLEMENT||||----------");
        }
        transactionSettlement.setDateModified(LocalDateTime.now());
        transactionSettlement.setModifiedBy(0L);
        transactionSettlementRepository.save(transactionSettlement);
    }

    private void preprocessSuccessfulSettlement(
            TransactionSettlement transactionSettlement, LocalDateTime dateSettled,
            BigDecimal totalFees, BigDecimal netAmount, BigDecimal grossAmount, LocalDateTime settlementInitiatedAt, Long userId) {
        transactionSettlement.setDateSettled(dateSettled);
        transactionSettlement.setTotalFee(totalFees);
        transactionSettlement.setSettlementNetAmount(netAmount);
        transactionSettlement.setSettlementGrossAmount(grossAmount);
        transactionSettlement.setSettlementStatus(SettlementStatus.SETTLED);
        transactionSettlement.setMerchantUserId(userId);
        transactionSettlement.setSettlementInitiationDate(settlementInitiatedAt);
        transactionSettlementRepository.save(transactionSettlement);
        log.info("--------||||COMPLETED PROCESSING MERCHANT SETTLEMENT TO {} ACCOUNT ||||----------", transactionSettlement.getAccountSettlementOption());
    }

    private void saveProcessSettledTransactions(List<PaymentGateway> paymentGateways, LocalDateTime dateSettled, String settlementRef) {
        paymentGateways.parallelStream().forEach(paymentGateway -> {
            paymentGateway.setSettlementStatus(SettlementStatus.SETTLED);
            paymentGateway.setSettlementDate(dateSettled);
            paymentGateway.setSettlementReferenceId(settlementRef);
        });
        paymentGatewayRepo.saveAllAndFlush(paymentGateways);
    }

    @Scheduled(cron = "* */30 * * * *")
    @SchedulerLock(name = "TaskScheduler_settleEveryFiveSeconds", lockAtLeastFor = "10s", lockAtMostFor = "15s")
    public void settleEveryFiveSeconds() {
        prorcessThirdPartyPaymentProcessed();
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

    @Scheduled(cron = "* */30 * * * *")
    @SchedulerLock(name = "TaskScheduler_expireTransactionAfterThirtyMinutes", lockAtLeastFor = "10s", lockAtMostFor = "30s")
    public void expireTransactionAfterThirtyMinutes() {
        wayaPaymentDAO.expireAllTransactionLessThan30Mins();
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
}
