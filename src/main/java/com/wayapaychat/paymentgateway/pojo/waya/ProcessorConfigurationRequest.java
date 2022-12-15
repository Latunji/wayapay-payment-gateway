package com.wayapaychat.paymentgateway.pojo.waya;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProcessorConfigurationRequest {
    private String name;
    private String code;
    private String description;
//    private String testBaseUrl;
//    private String liveBaseUrl;
    private Boolean cardAcquiring;
    private Boolean ussdAcquiring;
    private Boolean accountAcquiring;
    private Boolean payattitudeAcquiring;
}
