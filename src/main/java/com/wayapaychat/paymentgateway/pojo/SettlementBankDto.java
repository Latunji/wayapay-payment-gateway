package com.wayapaychat.paymentgateway.pojo;

import lombok.Data;

@Data
public class SettlementBankDto {

    private String merchantId;
    private String merchantSettlementBankAccountId;
    private String refNo;
    private String bankName;
    private String bankCode;
    private String accountNumber;
    private String accountName;
    private String dateCreated;
    private String dateModified;
    private Long createdBy;
    private Long modifiedBy;
    private boolean defaultSettlementAccount;
    private Boolean deleted;
    private String currency;
    private String mode;

}
