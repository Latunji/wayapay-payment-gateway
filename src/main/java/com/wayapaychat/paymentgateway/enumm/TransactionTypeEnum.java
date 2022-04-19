package com.wayapaychat.paymentgateway.enumm;

public enum TransactionTypeEnum {

    CARD("CARD"), MONEY("MONEY"), LOCAL("LOCAL"), TRANSFER("TRANSFER"), CASH("CASH"),
    BANK("BANK"), REVERSAL("REVERSAL"), WITHDRAW("WITHDRAW");

    private final String value;

    TransactionTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
