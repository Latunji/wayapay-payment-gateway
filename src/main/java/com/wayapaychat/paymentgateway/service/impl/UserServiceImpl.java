package com.wayapaychat.paymentgateway.service.impl;


import com.wayapaychat.paymentgateway.pojo.*;
import com.wayapaychat.paymentgateway.proxy.AuthApiClient;
import com.wayapaychat.paymentgateway.proxy.LoggingProxy;
import com.wayapaychat.paymentgateway.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

	private static AuthApiClient authApiClient;
	private final LoggingProxy loggingProxy;
	
	public ApiResponseBody<MyUserData> getUserDataByEmail(String email) {
		try{
			return authApiClient.getUserByEmail(email);
		}catch(Exception e){
			log.error("Call to get User by Email failing :: {}", e.getMessage());
			return new ApiResponseBody<>("Failure", false);
		}
	}
	
	public ApiResponseBody<MyUserData> getUserDataById(Long id) {
		try{
			return authApiClient.getUserById(id);
		}catch(Exception e){
			log.error("Call to get User by User Id failing :: {}", e.getMessage());
			return new ApiResponseBody<>("Failure", false);
		}
	}
	
	public ApiResponseBody<MyUserData> getUserDataByPhoneNumber(String phonenumber) {
		try{
			return authApiClient.getUserByPhoneNumber(phonenumber);
		}catch(Exception e){
			log.error("Call to get User by Phone Number failing :: {}", e.getMessage());
			return new ApiResponseBody<>("Failure", false);
		}
	}

	public void saveLog(LogRequest logPojo) {
		try{
			loggingProxy.saveNewLog(logPojo);
		}catch(Exception e){
			log.error("Error saving Logs:: {}", e.getMessage());
		}
	}
	
}
