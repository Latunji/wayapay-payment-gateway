package com.wayapaychat.paymentgateway.pojo.waya;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProcessorConfigurationRequest {
    private String cardAcquiring;
    private String ussdAcquiring;
    private String accountAcquiring;
    private String payattitudeAcquiring;
}
