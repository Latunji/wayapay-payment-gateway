package com.wayapaychat.paymentgateway.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class SettlementStatusUpdateDto {

    private List<TransactionSettlement> transactionSettlementList;
    private String merchantId;
}
