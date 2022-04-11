package com.wayapaychat.paymentgateway.repository;


import com.wayapaychat.paymentgateway.entity.TransactionSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Transactional
@Repository
public interface TransactionSettlementRepository extends JpaRepository<TransactionSettlement, Long> {
    @Query(value = "SELECT * FROM m_transaction_settlement WHERE deleted = false AND reference_id=?1", nativeQuery = true)
    Optional<TransactionSettlement> findByReferenceId(@NotNull String referenceId);
}
