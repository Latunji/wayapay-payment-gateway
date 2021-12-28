package com.wayapaychat.paymentgateway.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.wayapaychat.paymentgateway.entity.PaymentGateway;

public interface PaymentGatewayRepository extends JpaRepository<PaymentGateway, Long> {
	
	Optional<PaymentGateway> findByRefNo(String refNo);
	
	Optional<PaymentGateway> findByTranId(String tranId);
	
	@Query("SELECT u FROM PaymentGateway u " + "WHERE UPPER(u.tranId) = UPPER(:tranId) " + " AND u.del_flg = false"
			+  " AND u.tranDate = (:tranDate)")
	Optional<PaymentGateway> findByPaymentTrans(String tranId, LocalDate tranDate);

}
