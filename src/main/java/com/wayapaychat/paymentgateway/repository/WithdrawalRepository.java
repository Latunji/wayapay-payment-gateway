package com.wayapaychat.paymentgateway.repository;

import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.entity.Withdrawals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WithdrawalRepository extends JpaRepository<Withdrawals, Long> {

    @Query(value = "SELECT * FROM m_withdrawal WHERE withdrawal_status = 'SUCCESSFUL' " +
            "AND merchant_id=:merchantId ", nativeQuery = true)
    List<Withdrawals> findByWithdrawalStatus(String merchantId);
}
