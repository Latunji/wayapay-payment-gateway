package com.wayapaychat.paymentgateway.utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Random;

public class Utility {


    public static String transactionId() {
        Random rnd = new Random();
        String code = String.valueOf(001 + rnd.nextInt(999));
        DateFormat df = new SimpleDateFormat("yyMMddHHmmssss");
        String datePart = df.format(new java.util.Date());
        return code+datePart;// + next(2);
    }

    public static LocalDateTime currentAccurateTime(){
        return LocalDateTime.now().plusHours(1L);
    }
}
