package com.wayapaychat.paymentgateway.common.enums;

import lombok.Getter;

@Getter
public enum Interval {
    WEEKLY(7),
    MONTHLY(30),
    DAILY(1),
    ANNUALLY(365),
    BI_ANNUAL(730),
    QUARTERLY(90),
    SEMI_ANNUAL(180);
    final int days;

    Interval(int i) {
        days = i;
    }
}
