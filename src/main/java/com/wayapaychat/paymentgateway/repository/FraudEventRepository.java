package com.wayapaychat.paymentgateway.repository;

import com.wayapaychat.paymentgateway.entity.FraudEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

//@Transactional
//@Repository
public interface FraudEventRepository {
    @Query(value = "SELECT * FROM m_fraud_event WHERE deleted = false AND id=?1", nativeQuery = true)
    List<FraudEvent> findByEvenId(Long id);

    @Query(value = "SELECT * FROM m_fraud_event WHERE deleted = false", nativeQuery = true)
    Page<FraudEvent> findAllFraudEvents(Pageable pageable);
}
