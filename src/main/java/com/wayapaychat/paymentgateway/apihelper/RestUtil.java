package com.wayapaychat.paymentgateway.apihelper;

import org.springframework.http.HttpStatus;

/**
 *
 * This class is a error util for rest calls
 */

public class RestUtil {

    public static boolean isError(HttpStatus status) {
        HttpStatus.Series series = status.series();
        return (HttpStatus.Series.CLIENT_ERROR.equals(series)
                || HttpStatus.Series.SERVER_ERROR.equals(series));
    }
}
