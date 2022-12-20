package com.wayapaychat.paymentgateway.repository;

import com.wayapaychat.paymentgateway.entity.ChargedCard;
import com.wayapaychat.paymentgateway.entity.TokenizedCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<ChargedCard, Long> {

}
