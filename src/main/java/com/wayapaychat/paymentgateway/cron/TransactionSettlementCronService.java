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
import com.wayapaychat.paymentgateway.pojo.waya.wallet.DefaultWalletResponse;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.WalletSettlementResponse;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.WayaMerchantWalletSettlementPojo;
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
    @Value(value = "${waya.wallet.wayapay-debit-account}")
    private String debitWalletAccountNumber;


            @Scheduled(cron = "0 0 0 * * *")
//    @Scheduled(cron = "* */1 * * * *")
    @SchedulerLock(name = "TaskScheduler_createAndUpdateMerchantTransactionSettlement", lockAtLeastFor = "10s", lockAtMostFor = "30s")
    public void createAndUpdateMerchantTransactionSettlement() {
        LockAssert.assertLocked();
        List<MerchantUnsettledSuccessfulTransaction> merchantUnsettledSuccessfulTransactions = wayaPaymentDAO.merchantUnsettledSuccessTransactions(null);
        List<PaymentGateway> paymentGateways = paymentGatewayRepo.getAllTransactionNotSettled();
//        Map<String,List<PaymentGateway>> groupedMerchantTransactions = paymentGateways.parallelStream()
//                .collect(Collectors.groupingBy(PaymentGateway::getMerchantId));
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
                log.info("--------||||SUCCESSFULLY UPDATED NEXT MERCHANT TRANSACTION SETTLEMENT||||----------");
                processExpiredMerchantConfiguredSettlement(transactionSettlementRepository.save(transactionSettlement));
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
                log.info("--------||||SUCCESSFULLY CREATED MERCHANT TRANSACTION SETTLEMENT||||----------");
            }
        });
    }

    private void processExpiredMerchantConfiguredSettlement(TransactionSettlement transactionSettlement) {
        new Thread(() -> {
            try {
                LocalDateTime merchantConfiguredSettlementDate = transactionSettlement.getMerchantConfiguredSettlementDate();
                if (merchantConfiguredSettlementDate.isBefore(LocalDateTime.now()) || merchantConfiguredSettlementDate.isEqual(LocalDateTime.now())) {
                    log.info("--------||||PROCESSING MERCHANT TRANSACTION SETTLEMENT||||----------");
                    LocalDateTime settlementInitiatedAt = LocalDateTime.now();
                    String merchantId = transactionSettlement.getMerchantId();
                    //TODO: calculate the amount to settle from here

                    List<PaymentGateway> transactionsToSettle = paymentGatewayRepo.findAllNotSettled(merchantId);
                    BigDecimal grossAmount = transactionsToSettle.stream()
                            .map(PaymentGateway::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalFees = transactionsToSettle.stream()
                            .map(PaymentGateway::getFee)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal netAmount = grossAmount.subtract(totalFees);

                    @NotNull final String daemonToken = paymentGateWayCommonUtils.getDaemonAuthToken();
                    MerchantResponse merchantResponse = identityManager.getMerchantDetail(daemonToken, merchantId);
                    MerchantData merchantData = merchantResponse.getData();

                    if (transactionSettlement.getAccountSettlementOption().equals(AccountSettlementOption.BANK)) {
                        LocalDateTime dateSettled = LocalDateTime.now();
                        //TODO: Get the merchant settings and the bank account used for settlement
                        // Call the withdrawal endpoint to make payment to the user settlement account
                        saveProcessSettledTransactions(transactionsToSettle, dateSettled, transactionSettlement.getSettlementReferenceId());
                        preprocessSuccessfulSettlement(transactionSettlement, dateSettled, totalFees, netAmount, grossAmount, settlementInitiatedAt, merchantData.getUserId());
                    } else if (transactionSettlement.getAccountSettlementOption().equals(AccountSettlementOption.WALLET)) {

                        if (ObjectUtils.isNotEmpty(merchantData)) {
                            DefaultWalletResponse merchantDefaultWallet = walletProxy.getUserDefaultWalletAccount(paymentGateWayCommonUtils.getDaemonAuthToken(), merchantData.getUserId());
                            if (!merchantDefaultWallet.getStatus()) {
                                log.error("---------||||COULD NOT FETCH DEFAULT WALLET||||--------------: " + merchantDefaultWallet);
                                preprocessFailedSettlement(transactionSettlement);
                            } else {
                                LocalDateTime dateSettled = LocalDateTime.now();
                                @NotNull final String DEBIT_WALLET_ACCOUNT_NO = ObjectUtils.isEmpty(debitWalletAccountNumber) ? "NGN000016002001" : debitWalletAccountNumber;
                                @NotNull final String CREDIT_WALLET_ACCOUNT_NO = merchantDefaultWallet.getData().getAccountNo();
                                WayaMerchantWalletSettlementPojo walletCreditingRequest = WayaMerchantWalletSettlementPojo
                                        .builder()
                                        .amount(netAmount)
                                        .customerCreditAccount(CREDIT_WALLET_ACCOUNT_NO)
                                        .tranCrncy("NGN")
                                        .officeDebitAccount(DEBIT_WALLET_ACCOUNT_NO)
                                        .tranNarration(String.format("Settlement transaction for %s successful payment", transactionsToSettle.size())
                                        ).tranType("TRANSFER")
                                        .paymentReference(transactionSettlement.getSettlementReferenceId() + "_" + System.currentTimeMillis())
                                        .build();
                                WalletSettlementResponse walletSettlementResponse = walletProxy.creditMerchantDefaultWallet(
                                        paymentGateWayCommonUtils.getDaemonAuthToken(),
                                        walletCreditingRequest
                                );
                                log.info("-----||||WALLET SETTLEMENT RESPONSE FROM TEMPORAL SERVICE|||| {}----", walletSettlementResponse);
                                if (ObjectUtils.isNotEmpty(walletSettlementResponse.getData())) {
                                    log.info("----||||WALLET SETTLEMENT CREDITING TRANSACTION WAS SUCCESSFUL FROM[{}]" +
                                            " TO MERCHANT ACCOUNT NO[{}]||||----", DEBIT_WALLET_ACCOUNT_NO, merchantDefaultWallet.getData().getAccountNo());
                                    saveProcessSettledTransactions(transactionsToSettle, dateSettled, transactionSettlement.getSettlementReferenceId());
                                    preprocessSuccessfulSettlement(transactionSettlement, dateSettled, totalFees, netAmount, grossAmount, settlementInitiatedAt, merchantData.getUserId());
                                } else {
                                    preprocessFailedSettlement(transactionSettlement);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.info("------||||ERROR||||------", e);
                preprocessFailedSettlement(transactionSettlement);
            }
        }).start();
    }

    private void preprocessFailedSettlement(TransactionSettlement transactionSettlement) {
        if (transactionSettlement.getTotalRetrySettlementCount() < 6L) {
            transactionSettlement.setTotalRetrySettlementCount(transactionSettlement.getTotalRetrySettlementCount() + 1);
            transactionSettlement.setSettlementStatus(SettlementStatus.PENDING);
            log.info("--------||||FAILED PROCESSING MERCHANT SETTLEMENT... RESET TO PENDING||||----------");
        } else {
            transactionSettlement.setSettlementStatus(SettlementStatus.FAILED);
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
            paymentGateway.setSettlementDate(dateSettled.toLocalDate());
            paymentGateway.setSettlementReferenceId(settlementRef);
        });
        paymentGatewayRepo.saveAllAndFlush(paymentGateways);
    }

    @Scheduled(cron = "*/5 * * * * *")
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

    public void prorcessThirdPartyPaymentProcessed() {
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
