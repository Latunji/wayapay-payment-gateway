package com.wayapaychat.paymentgateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wayapaychat.paymentgateway.pojo.TokenCheckResponse;
import com.wayapaychat.paymentgateway.proxy.AuthApiClient;



@Component
public class GetUserDataService {

	@Autowired
	private AuthApiClient authProxy;
	
	public TokenCheckResponse getUserData(String token) {
		TokenCheckResponse res = authProxy.getUserDataToken(token);
		System.out.println("::::Token::::"+res.getMessage());
		System.out.println("::::Token::::"+res.getData().getEmail());
		return res;
	}
	
}
