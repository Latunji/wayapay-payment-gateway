package com.wayapaychat.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wayapaychat.paymentgateway.entity.PaymentWallet;

public interface PaymentWalletRepository extends JpaRepository<PaymentWallet, Long> {

}
