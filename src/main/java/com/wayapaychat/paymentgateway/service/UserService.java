package com.wayapaychat.paymentgateway.service;


import com.wayapaychat.paymentgateway.pojo.*;

public interface UserService {

    ApiResponseBody<AuthenticatedUser> getUserDataByEmail(String email);

    ApiResponseBody<AuthenticatedUser> getUserDataById(Long id);

    ApiResponseBody<AuthenticatedUser> getUserDataByPhoneNumber(String phonenumber);

    void saveLog(LogRequest logPojo);
}
