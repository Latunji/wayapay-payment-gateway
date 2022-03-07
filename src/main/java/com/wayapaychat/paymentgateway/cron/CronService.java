package com.wayapaychat.paymentgateway.cron;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.enumm.TransactionStatus;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaTransactionQuery;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import com.wayapaychat.paymentgateway.service.PaymentGatewayService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CronService {
	
	@Autowired
	PaymentGatewayRepository paymentGatewayRepo;
	
	@Autowired
	PaymentGatewayService paymentService;
	
	@Scheduled(cron = "*/5 * * * * *")
	public void PostUPCardSink() {
		List<PaymentGateway> product = paymentGatewayRepo.findAll();
		for(PaymentGateway payment : product) {
			PaymentGateway mPay = paymentGatewayRepo.findByRefNo(payment.getRefNo()).orElse(null);
				if(mPay != null && (!mPay.getStatus().name().equals("TRANSACTION_COMPLETED"))) {
					log.info("TRANSACTION STATUS: " + mPay.toString());
					if(!mPay.getTranId().isBlank() && StringUtils.isNumeric(mPay.getTranId())) {
						WayaTransactionQuery query = paymentService.GetTransactionStatus(mPay.getTranId());
						log.info("UP STATUS: " + query.toString());
						if(query.getStatus().contains("Approve")) {
							mPay.setStatus(TransactionStatus.TRANSACTION_COMPLETED);
						}else if(query.getStatus().contains("Reject")) {
							mPay.setStatus(TransactionStatus.TRANSACTION_FAILED);
						}
						paymentGatewayRepo.save(mPay);
					}
		            
		            
				}
			}
		}

}
