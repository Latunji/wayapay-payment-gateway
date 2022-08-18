package com.wayapaychat.paymentgateway.repository;

import com.wayapaychat.paymentgateway.entity.PaymentProcessor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface PaymentProcessorRepository extends JpaRepository<PaymentProcessor, Long>{
    @Query("SELECT * FROM m_payment_processor WHERE UPPER(name)=UPPER(:name)")
    Optional<PaymentProcessor> findByName(String name);

    Optional<PaymentProcessor> findByCode(String code);


}
