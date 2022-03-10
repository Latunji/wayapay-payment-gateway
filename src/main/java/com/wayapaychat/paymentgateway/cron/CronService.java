package com.wayapaychat.paymentgateway.cron;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.enumm.TransactionStatus;
import com.wayapaychat.paymentgateway.pojo.LoginRequest;
import com.wayapaychat.paymentgateway.pojo.PaymentData;
import com.wayapaychat.paymentgateway.pojo.TokenAuthResponse;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaTransactionQuery;
import com.wayapaychat.paymentgateway.pojo.waya.FundEventResponse;
import com.wayapaychat.paymentgateway.proxy.AuthApiClient;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import com.wayapaychat.paymentgateway.service.PaymentGatewayService;
import com.wayapaychat.paymentgateway.service.UnifiedPaymentProxy;

//import lombok.extern.slf4j.Slf4j;

@Service
//@Slf4j
public class CronService {

	@Autowired
	PaymentGatewayRepository paymentGatewayRepo;

	@Autowired
	PaymentGatewayService paymentService;
	
	@Autowired
	UnifiedPaymentProxy uniPayProxy;
	
	@Autowired
	AuthApiClient authProxy;
	
	@Value("${service.name}")
	private String username;

	@Value("${service.pass}")
	private String passSecret;

	@Scheduled(cron = "*/5 * * * * *")
	public void PostUPCardSink() {
		List<PaymentGateway> product = paymentGatewayRepo.findAll();
		for (PaymentGateway payment : product) {
			PaymentGateway mPay = paymentGatewayRepo.findByRefNo(payment.getRefNo()).orElse(null);
			if (mPay != null) {
				// log.info(mPay.toString());
				if (mPay.getStatus() != null) {
					if ((mPay.getStatus().compareTo(TransactionStatus.TRANSACTION_COMPLETED) != 0)
							&& (mPay.getStatus().compareTo(TransactionStatus.SUCCESSFUL) != 0)
							&& (mPay.getStatus().compareTo(TransactionStatus.FAILED) != 0)) {
						// log.info("TRANSACTION STATUS: " + mPay.toString());
						if (!mPay.getTranId().isBlank() && StringUtils.isNumeric(mPay.getTranId())) {
							WayaTransactionQuery query = paymentService.GetTransactionStatus(mPay.getTranId());
							// log.info("UP STATUS: " + query.toString());
							if (query.getStatus().contains("APPROVED")) {
								mPay.setStatus(TransactionStatus.TRANSACTION_COMPLETED);
								mPay.setSuccessfailure(true);
							} else if (query.getStatus().contains("REJECT")) {
								mPay.setStatus(TransactionStatus.TRANSACTION_FAILED);
								mPay.setSuccessfailure(false);
							}
							paymentGatewayRepo.save(mPay);
						}

					} else if (mPay.getStatus().compareTo(TransactionStatus.TRANSACTION_COMPLETED) == 0) {
						if (!mPay.getTranId().isBlank() && StringUtils.isNumeric(mPay.getTranId())) {
							WayaTransactionQuery query = paymentService.GetTransactionStatus(mPay.getTranId());
							mPay.setStatus(TransactionStatus.SUCCESSFUL);
							mPay.setSuccessfailure(true);
							if (query.getStatus().contains("APPROVED")) {
								mPay.setStatus(TransactionStatus.SUCCESSFUL);
								mPay.setSuccessfailure(true);
							} else if (query.getStatus().contains("REJECT")) {
								mPay.setStatus(TransactionStatus.FAILED);
								mPay.setSuccessfailure(false);
							}
							paymentGatewayRepo.save(mPay);
						}

					} else if (mPay.getStatus().compareTo(TransactionStatus.TRANSACTION_FAILED) == 0) {
						if (!mPay.getTranId().isBlank() && StringUtils.isNumeric(mPay.getTranId())) {
							WayaTransactionQuery query = paymentService.GetTransactionStatus(mPay.getTranId());
							mPay.setStatus(TransactionStatus.FAILED);
							mPay.setSuccessfailure(false);
							if (query.getStatus().contains("APPROVED")) {
								mPay.setStatus(TransactionStatus.SUCCESSFUL);
								mPay.setSuccessfailure(true);
							} else if (query.getStatus().contains("REJECT")) {
								mPay.setStatus(TransactionStatus.FAILED);
								mPay.setSuccessfailure(false);
							}
							paymentGatewayRepo.save(mPay);
						}
					} else if (mPay.getStatus().compareTo(TransactionStatus.TRANSACTION_PENDING) == 0) {
						if (!mPay.getTranId().isBlank() && StringUtils.isNumeric(mPay.getTranId())) {
							WayaTransactionQuery query = paymentService.GetTransactionStatus(mPay.getTranId());
							mPay.setStatus(TransactionStatus.PENDING);
							mPay.setSuccessfailure(false);
							if (query.getStatus().contains("APPROVED")) {
								mPay.setStatus(TransactionStatus.SUCCESSFUL);
								mPay.setSuccessfailure(true);
							} else if (query.getStatus().contains("REJECT")) {
								mPay.setStatus(TransactionStatus.FAILED);
								mPay.setSuccessfailure(false);
							}
							paymentGatewayRepo.save(mPay);
						}
					}
				}
			}
		}
	}
	
	@Scheduled(cron = "*/5 * * * * *")
	public void PostWalletTransactionSink() {
		List<PaymentGateway> payment = paymentGatewayRepo.findAll();
		for (PaymentGateway mPayment : payment) {
			
			if(!mPayment.isTranflg() && (mPayment.getStatus().compareTo(TransactionStatus.SUCCESSFUL) == 0)) {
				try {
					PaymentGateway sPayment = paymentGatewayRepo.findByRefNo(mPayment.getRefNo()).orElse(null);
					
					LoginRequest auth = new LoginRequest();
					auth.setEmailOrPhoneNumber(username);
					auth.setPassword(passSecret);
					TokenAuthResponse authToken = authProxy.UserLogin(auth);
					PaymentData payData = authToken.getData();
					String token = payData.getToken();
					
					FundEventResponse response = uniPayProxy.postTransactionPosition(token, mPayment);
					if(response.getPostedFlg() && (!response.getTranId().isBlank())) {
						sPayment.setTranflg(true);
						paymentGatewayRepo.save(sPayment);
					}
				}catch(Exception ex) {
					
				}
			}
			
		}
	}
}
