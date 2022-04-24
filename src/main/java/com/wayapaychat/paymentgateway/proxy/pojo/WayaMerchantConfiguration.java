package com.wayapaychat.paymentgateway.proxy.pojo;

import com.wayapaychat.paymentgateway.common.enums.Interval;
import com.wayapaychat.paymentgateway.enumm.AccountSettlementOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WayaMerchantConfiguration {
    private String merchantId;
    private String merchantConfigurationId;
    private Boolean emailMeForEveryTransaction;
    private Boolean emailCustomerForEveryTransaction;
    private String defaultCurrency;
    private Boolean enableCardPayments;
    private Boolean enableUSSDPayments;
    private Boolean enablePayAttitudePayments;
    private Boolean enableWalletPayments;
    private Boolean enableBankPayments;
    private Date placeSettlementOnHoldUntil;
    private Boolean settleToBankAccount;
    private Boolean settleToWalletAccount;
    private Boolean allowCustomersToCancelSubscriptions;
    private Boolean sendNotificationToBusinessEmailAddressOnly;
    private Boolean sendNotificationToAllTeamMembers;
    private Boolean sendNotificationToSpecificUsersOnly;
    private List<RecipientEmail> selectedUsersToReceiveEmailNotifications;
    private SettlementBankAccount settlementBankAccount;
    private Interval settlementInterval;
    private AccountSettlementOption transactionSettlementOption;
}
