package com.wayapaychat.paymentgateway.pojo;

import com.wayapaychat.paymentgateway.enumm.DeviceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.mobile.device.DevicePlatform;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DevicePojo {
    private DeviceType deviceType;
    private DevicePlatform platform;
    private String deviceInformation;
}
