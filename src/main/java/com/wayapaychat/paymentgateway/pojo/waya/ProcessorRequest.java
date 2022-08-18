package com.wayapaychat.paymentgateway.pojo.waya;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProcessorRequest {
    private String name;
    private String description;
}
