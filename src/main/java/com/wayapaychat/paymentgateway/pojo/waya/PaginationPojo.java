package com.wayapaychat.paymentgateway.pojo.waya;

import com.wayapaychat.paymentgateway.enumm.PaymentChannel;
import com.wayapaychat.paymentgateway.enumm.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationPojo {
    private Integer page;
    private Integer size;
    private Sort.Direction order;
    private String[] sortBy;
}
