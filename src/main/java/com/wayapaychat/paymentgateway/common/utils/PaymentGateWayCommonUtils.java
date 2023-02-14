package com.wayapaychat.paymentgateway.common.utils;

import com.wayapaychat.paymentgateway.enumm.DeviceType;
import com.wayapaychat.paymentgateway.exception.ApplicationException;
import com.wayapaychat.paymentgateway.pojo.waya.*;
import com.wayapaychat.paymentgateway.proxy.AuthApiClient;
import com.wayapaychat.paymentgateway.service.impl.MerchantProxy;
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
    private final VariableUtil variableUtil;
    private final AuthApiClient authApiClient;
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

    public static String getMerchantIdToUse(String merchantId, boolean required) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser();
        if (required && authenticatedUser.getAdmin() && ObjectUtils.isEmpty(merchantId))
            throw new ApplicationException(400, "01", "Okay! Please provide merchant id to proceed.");
        if (!authenticatedUser.getAdmin() && (ObjectUtils.isNotEmpty(merchantId) && !merchantId.equals(authenticatedUser.getMerchantId())))
            throw new ApplicationException(403, "01", "Oops! Sorry resource(s) can't be accessed");
        if (!authenticatedUser.getAdmin() && !authenticatedUser.isCorporate())
            throw new ApplicationException(403, "01", "Oops! Access not allowed!");
        if (!required && authenticatedUser.getAdmin() && ObjectUtils.isEmpty(merchantId))
            return merchantId;
        return ObjectUtils.isEmpty(merchantId) ? authenticatedUser.getMerchantId() : merchantId;
    }

    public static AuthenticatedUser getAuthenticatedUser() {
        return ((AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    public String getDaemonAuthToken() {
        TokenAuthResponse authToken = authApiClient.authenticateUser(
                LoginRequest.builder()
                        .password(variableUtil.getPassword())
                        .emailOrPhoneNumber(variableUtil.getUserName())
                        .build());
        if (!authToken.getStatus()) {
            log.info("------||||FAILED TO AUTHENTICATE DAEMON USER [email: {} , password: {}]||||--------",
                    variableUtil.getUserName(), variableUtil.getPassword());
            throw new ApplicationException(403, "01", "Failed to process user authentication...!");
        }
        PaymentData payData = authToken.getData();
        return payData.getToken();
    }
}
