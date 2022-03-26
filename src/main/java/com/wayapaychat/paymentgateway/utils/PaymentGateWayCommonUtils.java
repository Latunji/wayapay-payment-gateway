package com.wayapaychat.paymentgateway.utils;

import com.wayapaychat.paymentgateway.enumm.DeviceType;
import com.wayapaychat.paymentgateway.pojo.DevicePojo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DevicePlatform;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class PaymentGateWayCommonUtils {

    public static String getClientRequestIP(HttpServletRequest request) {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null)
            return xfHeader.split(",")[0];
        return request.getRemoteAddr();
    }

    public static String maskedPan(String pan) {
        String masked = "**** **** **** ";
        String maskedLastFourDigit = pan.substring(pan.length() - 4);
        return masked + maskedLastFourDigit;
    }

    public static DevicePojo getClientRequestDevice(Device device) {
        DeviceType deviceType;
        DevicePlatform platform;
        if (device.isNormal())
            deviceType = DeviceType.BROWSER;
        else if (device.isMobile())
            deviceType = DeviceType.MOBILE;
        else if (device.isTablet())
            deviceType = DeviceType.TABLET;
        else
            deviceType = DeviceType.BROWSER;
        platform = device.getDevicePlatform();
        return new DevicePojo(deviceType, platform, "");
    }
}
