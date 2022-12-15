package com.wayapaychat.paymentgateway.pojo.waya.wallet;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.wayapaychat.paymentgateway.enumm.CategoryType;
import com.wayapaychat.paymentgateway.enumm.TransactionTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WalletTransactionResponse {
    private Long id;
    private boolean del_flg;
    private boolean posted_flg;
    @NotNull
    private String tranId;
    @NotNull
    private String acctNum;
    @NotNull
    private BigDecimal tranAmount;
    @NotNull
    private TransactionTypeEnum tranType;
    @NotNull
    private String partTranType;
    @NotNull
    private String tranNarrate;

    @NotNull
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate tranDate;

    private String tranCrncyCode;
    private String paymentReference;
    private String tranGL;
    private Integer tranPart;
    private String relatedTransId;

    @ApiModelProperty(hidden = true)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;

    @ApiModelProperty(hidden = true)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updatedAt;

    private CategoryType tranCategory;
    private String createdBy;
    private String createdEmail;
    private String senderName;
    private String receiverName;
    private String transChannel;
    private boolean channel_flg;
}
