package com.wayapaychat.paymentgateway.repository;

import com.wayapaychat.paymentgateway.entity.SandboxRecurrentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Repository
public interface SandboxRecurrentTransactionRepository extends JpaRepository<SandboxRecurrentTransaction, Long> {
    @Query(value = "SELECT * FROM m_sandbox_recurrent_transaction WHERE deleted = false AND id=?1", nativeQuery = true)
    Optional<SandboxRecurrentTransaction> findRecurrentPaymentById(@NotNull Long id);

    @Query(value = "SELECT * FROM m_sandbox_recurrent_transaction WHERE deleted = false AND payment_link_id = ?2", nativeQuery = true)
    Page<SandboxRecurrentTransaction> findByPaymentLinkId(String paymentLinkId, Pageable pageable);

    @Query(value = "SELECT * FROM m_sandbox_recurrent_transaction WHERE deleted = false AND current_transaction_ref_no = ?1", nativeQuery = true)
    Optional<SandboxRecurrentTransaction> getByTransactionRef(String currentTransactionRefNo);

    @Query(value = "SELECT * FROM m_sandbox_recurrent_transaction WHERE deleted = false AND customer_id=:customerId AND merchant_id=:merchantId", nativeQuery = true)
    Page<SandboxRecurrentTransaction> getTransactionByCustomerId(String customerId, String merchantId, Pageable pageable);

    @Query(value = "SELECT * FROM m_sandbox_recurrent_transaction WHERE deleted = false AND recurrent_transaction_id=:recurrentTransactionId AND merchant_id=:merchantId", nativeQuery = true)
    Optional<SandboxRecurrentTransaction> getByRecurrentTransactionId(String recurrentTransactionId, String merchantId);

    @Query(value = "SELECT * FROM m_sandbox_recurrent_transaction WHERE deleted=false AND status='ACTIVE' AND CURRENT_TIMESTAMP >= CAST( next_charge_date AS TIMESTAMP ) ", nativeQuery = true)
    List<SandboxRecurrentTransaction> findNextRecurrentTransaction();

    @Query(value = "SELECT * FROM m_sandbox_recurrent_transaction WHERE deleted=false AND CURRENT_TIMESTAMP >= CAST( next_charge_date AS TIMESTAMP ) " +
            "AND status='AWAITING_PAYMENT'", nativeQuery = true)
    List<SandboxRecurrentTransaction> findAllAwaitingPayment();

    @Query(value = "SELECT * FROM m_sandbox_recurrent_transaction WHERE deleted=false AND CURRENT_TIMESTAMP >= CAST( next_charge_date AS TIMESTAMP ) " +
            "AND status='ACTIVE' AND max_charge_count = total_charge_count ", nativeQuery = true)
    List<SandboxRecurrentTransaction> findAllExpiredRecurrentTransaction();
}
