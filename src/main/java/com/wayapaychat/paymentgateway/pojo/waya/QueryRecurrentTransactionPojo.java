package com.wayapaychat.paymentgateway.pojo.waya;

import com.wayapaychat.paymentgateway.enumm.PaymentChannel;
import com.wayapaychat.paymentgateway.enumm.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryRecurrentTransactionPojo extends PaginationPojo {
    private TransactionStatus status;
    @NotNull(message = "customerId must not be null")
    private String customerId;
    private String merchantId;
    private PaymentChannel channel;
    @DateTimeFormat(pattern = "MM-dd-yyyy")
    private Date dateCreated;
}
