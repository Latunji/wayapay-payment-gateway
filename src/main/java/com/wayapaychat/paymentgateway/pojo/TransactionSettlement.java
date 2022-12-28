package com.wayapaychat.paymentgateway.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wayapaychat.paymentgateway.enumm.AccountSettlementOption;
import com.wayapaychat.paymentgateway.enumm.SettlementStatus;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
public class TransactionSettlement  {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime merchantConfiguredSettlementDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateSettled;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime settlementInitiationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateModified;

    private String merchantId;

    private Long merchantUserId;

    private SettlementStatus settlementStatus;

    private String settlementReferenceId;

    private BigDecimal settlementNetAmount = BigDecimal.ZERO;

    private BigDecimal settlementGrossAmount = BigDecimal.ZERO;

    private BigDecimal totalFee = BigDecimal.ZERO;

    private String settlementAccount;

    private AccountSettlementOption accountSettlementOption;

    private String settlementBeneficiaryAccount;
    private Long totalRetrySettlementCount;

    private String paymentRefNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateCreated;

    private Long createdBy;

    private Long modifiedBy;

    private Boolean deleted = false;



    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}