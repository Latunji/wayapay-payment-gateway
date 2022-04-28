package com.wayapaychat.paymentgateway.repository;

import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentGatewayRepository extends JpaRepository<PaymentGateway, Long> {

    Optional<PaymentGateway> findByRefNo(String refNo);

    Optional<PaymentGateway> findByTranId(String tranId);

    @Query("SELECT u FROM PaymentGateway u " + "WHERE UPPER(u.tranId) = UPPER(:tranId) " + " AND u.del_flg = false"
            + " AND u.tranDate = (:tranDate)")
    Optional<PaymentGateway> findByPaymentTrans(String tranId, LocalDate tranDate);

    @Query("SELECT u FROM PaymentGateway u " + "WHERE UPPER(u.refNo) = UPPER(:ref) " + " AND u.del_flg = false"
            + " AND u.merchantId = (:merchId)")
    Optional<PaymentGateway> findByRefMerchant(String ref, String merchId);

    @Query(value = "select * from m_payment_gateway where del_flg = false and customer_name != '' and status != ''", nativeQuery = true)
    List<PaymentGateway> findByPayment();

    @Query(value = "select * from m_payment_gateway where merchant_id = :mechtId and del_flg = false and customer_name != '' and status != '' " +
            " ORDER BY rcre_time DESC", nativeQuery = true)
    List<PaymentGateway> findByMerchantPayment(String mechtId);

    @Query(value = "SELECT * FROM m_payment_gateway WHERE del_flg = false " +
            " AND customer_id=:customerId AND merchant_id=:merchantId ", nativeQuery = true)
    Page<PaymentGateway> findByCustomerId(String customerId, String merchantId, Pageable pageable);

    @Query(value = "SELECT * FROM m_payment_gateway WHERE del_flg = false " +
            " AND status=:status AND customer_id=:customerId AND merchant_id=:merchantId", nativeQuery = true)
    Page<PaymentGateway> findByStatus(String customerId, String merchantId, String status, Pageable pageable);

    @Query(value = "SELECT * FROM m_payment_gateway WHERE del_flg = false " +
            " AND channel=:channel AND customer_id=:customerId AND merchant_id=:merchantId", nativeQuery = true)
    Page<PaymentGateway> findByCustomerIdChannel(String customerId, String merchantId, String channel, Pageable pageable);

    @Query(value = "SELECT * FROM m_payment_gateway WHERE del_flg = false " +
            " AND status=:status AND channel=:channel AND customer_id=:customerId AND merchant_id=:merchantId", nativeQuery = true)
    Page<PaymentGateway> findByCustomerIdChannelStatus(String customerId, String merchantId, String status, String channel, Pageable pageable);

    @Query(value = "SELECT * FROM m_payment_gateway WHERE tranflg = false AND ( status = 'SUCCESSFUL' OR status = 'SUCCESSFUL' ) ", nativeQuery = true)
    List<PaymentGateway> findAllNotFlaggedAndSuccessful();

    @Query(value = "SELECT * FROM m_payment_gateway WHERE del_flg=false AND status = 'SUCCESSFUL' AND settlement_status='PENDING' AND merchant_id=?1", nativeQuery = true)
    List<PaymentGateway> findAllNotSettled(String merchantId);

    @Query(value = "SELECT * FROM m_payment_gateway WHERE del_flg=false AND status = 'SUCCESSFUL' AND settlement_status='PENDING'", nativeQuery = true)
    List<PaymentGateway> getAllTransactionNotSettled();

    @Query(value = "SELECT * FROM m_payment_gateway WHERE del_flg=false AND status = 'SUCCESSFUL' AND settlement_status='PENDING' AND merchant_id=:merchantId ", nativeQuery = true)
    List<PaymentGateway> getAllTransactionNotSettled(String merchantId);
}
