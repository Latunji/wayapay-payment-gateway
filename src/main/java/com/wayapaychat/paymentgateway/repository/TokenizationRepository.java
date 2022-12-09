package com.wayapaychat.paymentgateway.repository;

import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.entity.TokenizedCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface TokenizationRepository extends JpaRepository<TokenizedCard,Long> {

    Optional<TokenizedCard> findToken (String customerId, String merchantId);
}
