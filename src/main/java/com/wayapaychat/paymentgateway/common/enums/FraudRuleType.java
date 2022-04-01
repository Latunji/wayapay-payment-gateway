package com.wayapaychat.paymentgateway.common.enums;

public enum FraudRuleType {

    SAME_IP_THREE_TIMES_TRANSACTION_IN_ONE_HOUR("" +
            "Multiple (3 times) transactions from Same IP within one hour " +
            "- Either with same email or different emails, " +
            "Same device signature or different device signatures, same pan or different pan. ", FraudRuleType.ACTION_ONE),

    INCORRECT_PASSWORD_ENTERED("Multiple (3 times)  transactions from different IP within one hour " +
            "- Either with same email or different emails, " +
            "Same device signature or different device signatures, same pan or different pan. ", FraudRuleType.ACTION_ONE);

    private static final String ACTION_ONE = "All details used are initially blocked for 1 hour. " +
            "(Email, device signature, Pan used, IP, phone number). " +
            "After suspension has been lifted, if the same rule is flaunted again, the previous suspension time is multiplied " +
            "by 2 and subsequently. Report to Admin Notification Page for further review ";
    private final String rule;
    private final String action;

    FraudRuleType(String rule, String action) {
        this.rule = rule;
        this.action = action;
    }

    public String getRule() {
        return rule;
    }

    public String getAction() {
        return action;
    }
}
