package com.wayapaychat.paymentgateway.pojo.waya;

import com.wayapaychat.paymentgateway.enumm.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementQueryPojo {
    private String referenceId;
    private SettlementStatus status;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startSettlementDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endSettlementDate;
}
