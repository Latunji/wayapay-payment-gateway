package com.wayapaychat.paymentgateway.repository;

import com.wayapaychat.paymentgateway.entity.RecurrentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Repository
public interface RecurrentTransactionRepository extends JpaRepository<RecurrentTransaction, Long> {
    @Query(value = "SELECT * FROM m_recurrent_transaction WHERE deleted = false AND id=?1", nativeQuery = true)
    Optional<RecurrentTransaction> findRecurrentPaymentById(@NotNull Long id);

    @Query(value = "SELECT * FROM m_recurrent_transaction WHERE deleted = false AND payment_link_id = ?2", nativeQuery = true)
    Page<RecurrentTransaction> findByPaymentLinkId(String paymentLinkId, Pageable pageable);

    @Query(value = "SELECT * FROM m_recurrent_transaction WHERE deleted = false AND current_transaction_ref_no = ?1", nativeQuery = true)
    Optional<RecurrentTransaction> getByTransactionRef(String currentTransactionRefNo);

    @Query(value = "SELECT * FROM m_recurrent_transaction WHERE deleted = false AND customer_id=:customerId AND merchant_id=:merchantId", nativeQuery = true)
    Page<RecurrentTransaction> getTransactionByCustomerId(String customerId, String merchantId, Pageable pageable);

    @Query(value = "SELECT * FROM m_recurrent_transaction WHERE deleted = false AND recurrent_transaction_id=:recurrentTransactionId AND merchant_id=:merchantId", nativeQuery = true)
    Optional<RecurrentTransaction> getByRecurrentTransactionId(String recurrentTransactionId, String merchantId);
}
