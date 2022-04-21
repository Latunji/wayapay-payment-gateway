package com.wayapaychat.paymentgateway.repository;

import com.wayapaychat.paymentgateway.entity.FraudRule;
import com.wayapaychat.paymentgateway.common.enums.FraudRuleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

//@Transactional
//@Repository
public interface FraudRuleRepository {
    @Query(value = "SELECT * FROM m_fraud_event WHERE deleted = false AND id=?1", nativeQuery = true)
    List<FraudRule> findByEvenId(Long id);

    @Query(value = "SELECT * FROM m_fraud_rule WHERE deleted = false", nativeQuery = true)
    Page<FraudRule> findAllFraudEvents(Pageable pageable);

    @Query(value = "SELECT * FROM m_fraud_rule WHERE deleted = false AND fraud_rule_type=?", nativeQuery = true)
    FraudRule findByFraudRuleType(FraudRuleType fraudRuleType);
}

