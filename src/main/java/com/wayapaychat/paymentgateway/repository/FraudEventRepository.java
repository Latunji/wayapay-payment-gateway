package com.wayapaychat.paymentgateway.repository;

import com.wayapaychat.paymentgateway.entity.FraudEvent;
import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface FraudEventRepository extends JpaRepository<FraudEvent, Long> {
    @Query(value = "SELECT * FROM m_fraud_event WHERE deleted = false AND id=?1", nativeQuery = true)
    List<FraudEvent> findByEvenId(Long id);

    @Query(value = "SELECT * FROM m_fraud_event WHERE deleted = false", nativeQuery = true)
    Page<FraudEvent> findAllFraudEvents(Pageable pageable);
}
