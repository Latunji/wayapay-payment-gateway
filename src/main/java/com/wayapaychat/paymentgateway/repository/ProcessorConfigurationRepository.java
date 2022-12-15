package com.wayapaychat.paymentgateway.repository;

import com.wayapaychat.paymentgateway.entity.ProcessorConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessorConfigurationRepository extends JpaRepository<ProcessorConfiguration, Long>{
    @Query(value = "SELECT * FROM m_processor_configuration WHERE UPPER(name)=UPPER(:name)", nativeQuery = true)
    Optional<ProcessorConfiguration> findByName(String name);

    Optional<ProcessorConfiguration> findByCode(String code);

    @Query(value = "SELECT * FROM m_processor_configuration WHERE UPPER(name)=UPPER(:nameOrCode) OR UPPER(name)=UPPER(:nameOrCode)", nativeQuery = true)
    Optional<ProcessorConfiguration> findByNameOrCode(String nameOrCode);
}
