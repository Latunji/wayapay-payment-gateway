/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.wayapaychat.paymentgateway.repository;

import com.wayapaychat.paymentgateway.entity.TokenizedCard;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author oluwatosin
 */
public interface TokenizeRepository extends JpaRepository<TokenizedCard, Long>{
    
       @Query(value = "SELECT * FROM m_tokenized_card WHERE customer_id=:customerId AND merchant_id=:merchantId ", nativeQuery = true)
    Optional<TokenizedCard> findByRefMerchant(String customerId, String merchantId);

       Optional<TokenizedCard> findByCustomerIdAndCardNumber(String customerId, String cardNumber);
    
}
