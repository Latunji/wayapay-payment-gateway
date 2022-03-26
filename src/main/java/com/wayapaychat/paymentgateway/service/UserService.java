package com.wayapaychat.paymentgateway.service;


import com.wayapaychat.paymentgateway.pojo.*;

public interface UserService {

    ApiResponseBody<MyUserData> getUserDataByEmail(String email);

    ApiResponseBody<MyUserData> getUserDataById(Long id);

    ApiResponseBody<MyUserData> getUserDataByPhoneNumber(String phonenumber);

    void saveLog(LogRequest logPojo);
}
