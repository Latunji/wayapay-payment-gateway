package com.wayapaychat.paymentgateway.repository;

import com.wayapaychat.paymentgateway.entity.RecurrentPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Repository
public interface RecurrentPaymentRepository extends JpaRepository<RecurrentPayment, Long> {
    @Query(value = "SELECT * FROM m_recurrent_payment WHERE deleted = false AND id=?1", nativeQuery = true)
    Optional<RecurrentPayment> findRecurrentPaymentById(@NotNull Long id);

    @Query(value = "SELECT * FROM m_recurrent_payment WHERE deleted = false AND payment_link_id = ?2", nativeQuery = true)
    Page<RecurrentPayment> findByPaymentLinkId(String paymentLinkId, Pageable pageable);

    @Query(value = "SELECT * FROM m_recurrent_payment WHERE deleted = false AND current_transaction_ref_no = ?1", nativeQuery = true)
    Optional<RecurrentPayment> getByTransactionRef(String currentTransactionRefNo);
}
