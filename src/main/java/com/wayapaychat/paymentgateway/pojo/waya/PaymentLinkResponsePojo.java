package com.wayapaychat.paymentgateway.pojo.waya;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.wayapaychat.paymentgateway.common.enums.Interval;
import com.wayapaychat.paymentgateway.common.enums.PaymentLinkType;
import com.wayapaychat.paymentgateway.pojo.MerchantData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLinkResponsePojo {
    private String code;
    private String date;
    private String message;
    private PaymentLinkResponse data;
}
