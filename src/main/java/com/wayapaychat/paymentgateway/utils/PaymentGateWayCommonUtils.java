package com.wayapaychat.paymentgateway.utils;

import com.wayapaychat.paymentgateway.enumm.DeviceType;
import com.wayapaychat.paymentgateway.exception.ApplicationException;
import com.wayapaychat.paymentgateway.pojo.DevicePojo;
import com.wayapaychat.paymentgateway.pojo.MerchantData;
import com.wayapaychat.paymentgateway.pojo.MyUserData;
import com.wayapaychat.paymentgateway.service.MerchantProxy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DevicePlatform;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@AllArgsConstructor
@Slf4j
public class PaymentGateWayCommonUtils {
    private final MerchantProxy merchantProxy;

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

    public String validateUserAndGetMerchantId(String merchantId) {
//        MyUserData user = getAuthenticatedUser();
//        if (!user.isEmailVerified())
//            throw new ApplicationException(403, "01", "Account needs email verification");
////        if (!user.isPhoneVerified())
////            throw new ApplicationException(403, "01", "Account needs email verification");
//        else if (!user.getAdmin() && ObjectUtils.isNotEmpty(merchantId))
//            throw new ApplicationException(403, "01", "Oops! Operation not allowed");
//        else if (!user.isCorporate() && ObjectUtils.isEmpty(merchantId))
//            throw new ApplicationException(403, "01", "Oops! Operation allowed to only corporate account");
//        if (user.getAdmin() && ObjectUtils.isNotEmpty(merchantId))
//            return merchantId;
//        else {
//            MerchantData merchantResponse = this.merchantProxy.getMerchantAccount().getData();
//            if (ObjectUtils.isEmpty(merchantResponse))
//                throw new ApplicationException(403, "01", "Oops! Merchant account not found");
//            return merchantResponse.getMerchantId();
//        }
        return merchantId;
    }

    public MyUserData getAuthenticatedUser() {
        return ((MyUserData) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
