package com.wayapaychat.paymentgateway.repository;

import com.wayapaychat.paymentgateway.entity.SandboxPaymentGateway;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SandboxPaymentGatewayRepository extends JpaRepository<SandboxPaymentGateway, Long> {

    Optional<SandboxPaymentGateway> findByRefNo(String refNo);

    Optional<SandboxPaymentGateway> findByTranId(String tranId);

    @Query("SELECT u FROM PaymentGateway u " + "WHERE UPPER(u.tranId) = UPPER(:tranId) " + " AND u.del_flg = false"
            + " AND u.tranDate = (:tranDate)")
    Optional<SandboxPaymentGateway> findByPaymentTrans(String tranId, LocalDate tranDate);

    @Query("SELECT u FROM PaymentGateway u " + "WHERE UPPER(u.refNo) = UPPER(:ref) " + " AND u.del_flg = false"
            + " AND u.merchantId = (:merchId)")
    Optional<SandboxPaymentGateway> findByRefMerchant(String ref, String merchId);

    @Query(value = "select * from m_sandbox_payment_gateway where del_flg = false and customer_name != '' and status != ''", nativeQuery = true)
    List<SandboxPaymentGateway> findByPayment();

    @Query(value = "select * from m_sandbox_payment_gateway WHERE merchant_id =:mechtId AND del_flg = false ORDER BY rcre_time DESC", nativeQuery = true)
    List<SandboxPaymentGateway> findByMerchantPayment(String mechtId);

    @Query(value = "SELECT * FROM m_sandbox_payment_gateway WHERE merchant_id IS NOT NULL AND del_flg = false ORDER BY rcre_time DESC", nativeQuery = true)
    List<SandboxPaymentGateway> findByMerchantPayment();

    @Query(value = "SELECT * FROM m_sandbox_payment_gateway WHERE del_flg = false " +
            " AND customer_id=:customerId AND merchant_id=:merchantId ", nativeQuery = true)
    Page<SandboxPaymentGateway> findByCustomerId(String customerId, String merchantId, Pageable pageable);

    @Query(value = "SELECT * FROM m_sandbox_payment_gateway WHERE del_flg = false " +
            " AND status=:status AND customer_id=:customerId AND merchant_id=:merchantId", nativeQuery = true)
    Page<SandboxPaymentGateway> findByStatus(String customerId, String merchantId, String status, Pageable pageable);

    @Query(value = "SELECT * FROM m_sandbox_payment_gateway WHERE del_flg = false " +
            " AND channel=:channel AND customer_id=:customerId AND merchant_id=:merchantId", nativeQuery = true)
    Page<SandboxPaymentGateway> findByCustomerIdChannel(String customerId, String merchantId, String channel, Pageable pageable);

    @Query(value = "SELECT * FROM m_sandbox_payment_gateway WHERE del_flg = false " +
            " AND status=:status AND channel=:channel AND customer_id=:customerId AND merchant_id=:merchantId", nativeQuery = true)
    Page<SandboxPaymentGateway> findByCustomerIdChannelStatus(String customerId, String merchantId, String status, String channel, Pageable pageable);

    @Query(value = "SELECT * FROM m_sandbox_payment_gateway WHERE tranflg = false AND ( status = 'SUCCESSFUL' OR status = 'SUCCESSFUL' ) ", nativeQuery = true)
    List<SandboxPaymentGateway> findAllNotFlaggedAndSuccessful();

    @Query(value = "SELECT * FROM m_sandbox_payment_gateway WHERE del_flg=false AND status = 'SUCCESSFUL' AND settlement_status='PENDING' AND merchant_id=?1", nativeQuery = true)
    List<SandboxPaymentGateway> findAllNotSettled(String merchantId);

    @Query(value = "SELECT * FROM m_sandbox_payment_gateway WHERE del_flg=false AND status = 'SUCCESSFUL' AND settlement_status='PENDING'", nativeQuery = true)
    List<SandboxPaymentGateway> getAllTransactionNotSettled();

    @Query(value = "SELECT * FROM m_sandbox_payment_gateway WHERE del_flg=false AND status = 'SUCCESSFUL' AND settlement_status='PENDING' AND merchant_id=:merchantId ", nativeQuery = true)
    List<SandboxPaymentGateway> getAllTransactionNotSettled(String merchantId);

    @Query(value = "SELECT * FROM m_sandbox_payment_gateway WHERE del_flg=false AND (status = 'PENDING' OR status='FAILED') AND transaction_expired=false " +
            " AND (channel = 'CARD' OR channel='PAYATTITUDE') ", nativeQuery = true)
    List<SandboxPaymentGateway> findAllFailedAndPendingTransactions();

    @Query(value = "SELECT * FROM m_sandbox_payment_gateway WHERE payment_link=:paymentLinkId AND merchant_id=:merchantId AND del_flg=false ", nativeQuery = true)
    Page<SandboxPaymentGateway> getAllByPaymentLinkId(String merchantId, String paymentLinkId, Pageable pageable);

    @Query(value = "SELECT * FROM m_sandbox_payment_gateway WHERE payment_link=:paymentLinkId AND del_flg=false ", nativeQuery = true)
    Page<SandboxPaymentGateway> getAllByPaymentLinkId(String paymentLinkId, Pageable pageable);

    @Query(value = "SELECT * FROM m_sandbox_payment_gateway WHERE ref_no IN (?1)", nativeQuery = true)
    List<SandboxPaymentGateway> findAllByDelimiters(String delimitedRefNo);

    @Query(value = "SELECT * FROM m_sandbox_payment_gateway WHERE settlement_reference_id=:settlementReferenceId AND del_flg=false ", nativeQuery = true)
    Optional<SandboxPaymentGateway> getTransactionSettlementBySettlementReferenceId(String settlementReferenceId);
}
