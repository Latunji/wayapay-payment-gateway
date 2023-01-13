package com.wayapaychat.paymentgateway.repository;

import com.wayapaychat.paymentgateway.entity.SandboxTransactionSettlement;
import com.wayapaychat.paymentgateway.entity.TransactionSettlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public interface SandboxTransactionSettlementRepository extends JpaRepository<SandboxTransactionSettlement, Long> {
    @Query(value = "SELECT * FROM m_sandbox_transaction_settlement WHERE deleted = false AND reference_id=?1 AND merchant_id=?2", nativeQuery = true)
    Optional<SandboxTransactionSettlement> findByReferenceId(@NotNull String merchantId, @NotNull String referenceId);

    @Query(value = "SELECT * FROM m_sandbox_transaction_settlement WHERE deleted = false AND merchant_id=?1 AND settlement_status=?2 ", nativeQuery = true)
    Page<SandboxTransactionSettlement> findAllWithStatus(String merchantId, String status, Pageable pageable);

    @Query(value = "SELECT * FROM m_sandbox_transaction_settlement WHERE deleted = false AND merchant_id=?1 AND settlement_status=?2 " +
            " AND CAST(date_settled AS TIMESTAMP) >= CAST(?3 AS TIMESTAMP) ", nativeQuery = true)
    Page<SandboxTransactionSettlement> findAllWithSettlementDateStatus(String merchantId, String status, Date startSettlementDate, Pageable pageable);

    @Query(value = "SELECT * FROM m_sandbox_transaction_settlement WHERE deleted = false AND merchant_id=?1 AND settlement_status=?2 " +
            " AND CAST(date_settled AS TIMESTAMP) BETWEEN CAST(?3 AS TIMESTAMP) AND CAST(?4 AS TIMESTAMP) ", nativeQuery = true)
    Page<SandboxTransactionSettlement> findAllWithStartEndDatesStatus(String merchantId, String name, Date startSettlementDate, Date endSettlementDate, Pageable pageable);

    @Query(value = "SELECT * FROM m_sandbox_transaction_settlement WHERE deleted = false AND merchant_id=?1 AND CAST(date_settled AS TIMESTAMP) " +
            " BETWEEN CAST(?3 AS TIMESTAMP) AND CAST(?4 AS TIMESTAMP) ", nativeQuery = true)
    Page<SandboxTransactionSettlement> findAllWithStartEndDates(String merchantId, Date startSettlementDate, Date endSettlementDate, Pageable pageable);

    @Query(value = "SELECT * FROM m_sandbox_transaction_settlement WHERE deleted = false AND settlement_status = 'PENDING' ", nativeQuery = true)
    List<SandboxTransactionSettlement> findAllMerchantSettlementPending();

//    @Query(value = "SELECT * FROM m_sandbox_transaction_settlement WHERE merchant_id=:merchantId ", nativeQuery = true)
//    Page<SandboxTransactionSettlement> findAll(String merchantId, Pageable pageable);

    @Query(value = "SELECT * FROM m_sandbox_transaction_settlement WHERE deleted = false AND merchant_id=:merchantId ", nativeQuery = true)
    Page<SandboxTransactionSettlement> findAll(String merchantId, Pageable pageable);

    @Query(value = "SELECT * FROM m_sandbox_transaction_settlement WHERE deleted = false AND settlement_reference_id=:settlementReferenceId ", nativeQuery = true)
    Optional<SandboxTransactionSettlement> getTransactionSettlementBySettlementReferenceId(String settlementReferenceId);
}
