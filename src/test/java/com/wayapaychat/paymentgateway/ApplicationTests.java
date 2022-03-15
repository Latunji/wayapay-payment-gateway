package com.wayapaychat.paymentgateway;

import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.enumm.PaymentChannel;
import com.wayapaychat.paymentgateway.enumm.TransactionStatus;
import com.wayapaychat.paymentgateway.listener.PaymentGatewayEntityLifeCircle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
class ApplicationTests {
    @Autowired
    private PaymentGatewayEntityLifeCircle paymentGatewayEntityLifeCircle;

    @Test
    void contextLoads() {
    }

    @Test
    void testSendTransactionEmail() throws Exception {
        paymentGatewayEntityLifeCircle.sendTransactionNotificationAfterPaymentIsSuccessful(
                PaymentGateway
                        .builder()
                        .amount(new BigDecimal("90000.0000"))
                        .cardNo("Testing card")
                        .channel(PaymentChannel.USSD)
                        .currencyCode("971")
                        .customerName("Adeshina")
                        .customerPhone("+2348160110719")
                        .del_flg(false)
                        .description("I am just testing this payment out")
                        .fee(new BigDecimal("0.00"))
                        .encyptCard("test")
                        .merchantName("Adeshina Merchant")
                        .returnUrl("https://google.com")
                        .preferenceNo("test_refere" + LocalDateTime.now())
                        .status(TransactionStatus.SUCCESSFUL)
                        .scheme("card")
                        .tranDate(LocalDate.now())
                        .successfailure(true)
                        .vendorDate(LocalDate.now())
                        .refNo(LocalDateTime.now().toString())
                        .merchantId("MER_mrereawesfdf")
                        .customerEmail("test-email@gmail.com")
                        .merchantEmail("test-email@gmail.com")
                        .tranId(LocalDateTime.now().toString())
                        .id(1L)
                        .build()
        );
    }
}
