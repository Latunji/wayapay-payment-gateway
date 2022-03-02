package com.wayapaychat.paymentgateway.repository;

import java.time.LocalDate;
import java.util.List;
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
	
	@Query("SELECT u FROM PaymentGateway u " + "WHERE UPPER(u.refNo) = UPPER(:ref) " + " AND u.del_flg = false"
			+  " AND u.merchantId = (:merchId)")
	Optional<PaymentGateway> findByRefMerchant(String ref, String merchId);
	
	@Query(value ="select * from m_payment_gateway where del_flg = false and customer_name != '' and status != ''", nativeQuery = true)
	List<PaymentGateway> findByPayment();

}
