package com.wayapaychat.paymentgateway.repository;


import com.wayapaychat.paymentgateway.entity.SettlementPricingConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Transactional
@Repository
public interface SettlementPricingConfigurationRepository extends JpaRepository<SettlementPricingConfiguration, Long> {
    @Query(value = "SELECT * FROM m_settlement_pricing_configuration WHERE deleted = false AND reference_id=?1", nativeQuery = true)
    Optional<SettlementPricingConfiguration> findByReferenceId(@NotNull String referenceId);
}
