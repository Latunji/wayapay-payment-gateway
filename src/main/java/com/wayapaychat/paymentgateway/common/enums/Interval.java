package com.wayapaychat.paymentgateway.common.enums;

import lombok.Getter;

@Getter
public enum Interval {
    WEEKLY(7),
    MONTHLY(30),
    DAILY(1),
    FIVE_DAYS(5),
    TWO_DAYS(2),
    ZERO_DAYS(0),
    ANNUALLY(365),
    BI_ANNUAL(730),
    INSTANTLY(0),
    QUARTERLY(90),
    SEMI_ANNUAL(180);
    final int days;

    Interval(int i) {
        days = i;
    }
}
