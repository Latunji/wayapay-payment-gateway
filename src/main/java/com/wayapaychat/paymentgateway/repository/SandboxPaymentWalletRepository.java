package com.wayapaychat.paymentgateway.repository;

import com.wayapaychat.paymentgateway.entity.SandboxPaymentWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SandboxPaymentWalletRepository extends JpaRepository<SandboxPaymentWallet,Long> {

}