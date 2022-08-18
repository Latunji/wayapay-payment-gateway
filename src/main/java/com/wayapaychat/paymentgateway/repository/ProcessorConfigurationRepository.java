package com.wayapaychat.paymentgateway.repository;

import com.wayapaychat.paymentgateway.entity.ProcessorConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessorConfigurationRepository extends JpaRepository<ProcessorConfiguration, Long>{
    @Query("SELECT * FROM m_processor_configuration LIMIT 1")
    ProcessorConfiguration getProcessorConfigurations();
}
