package com.wayapaychat.paymentgateway.service;


import com.wayapaychat.paymentgateway.pojo.waya.ApiResponseBody;
import com.wayapaychat.paymentgateway.pojo.waya.AuthenticatedUser;
import com.wayapaychat.paymentgateway.pojo.waya.LogRequest;

public interface UserService {

    ApiResponseBody<AuthenticatedUser> getUserDataByEmail(String email);

    ApiResponseBody<AuthenticatedUser> getUserDataById(Long id);

    ApiResponseBody<AuthenticatedUser> getUserDataByPhoneNumber(String phonenumber);

    void saveLog(LogRequest logPojo);
}
