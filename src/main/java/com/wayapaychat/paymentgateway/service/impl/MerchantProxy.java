package com.wayapaychat.paymentgateway.service.impl;

import com.wayapaychat.paymentgateway.config.FeignClientInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantResponse;
import com.wayapaychat.paymentgateway.proxy.IdentityManagementServiceProxy;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MerchantProxy {

	@Autowired
	IdentityManagementServiceProxy identManager;

	public MerchantResponse getMerchantInfo(String token, String id) {
		try {

			MerchantResponse receiptResponse = identManager.getMerchantDetail(token, id);
			if (receiptResponse == null)
				return null;

			log.info("Receipt: {}", receiptResponse);

			if (receiptResponse.getCode().equals("00"))
				return receiptResponse;

		} catch (Exception ex) {
			if (ex instanceof FeignException) {
				String httpStatus = Integer.toString(((FeignException) ex).status());
				log.error("Feign Exception Status {}", httpStatus);
			}
			log.error("Higher Wahala {}", ex.getMessage());
		}
		return null;
	}

	public MerchantResponse getMerchantAccount() {
		return identManager.getMerchantAccount(FeignClientInterceptor.getBearerTokenHeader());
	}
}
