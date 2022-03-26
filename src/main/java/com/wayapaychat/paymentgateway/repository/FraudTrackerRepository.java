package com.wayapaychat.paymentgateway.repository;

import com.wayapaychat.paymentgateway.entity.FraudTracker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

//@Transactional
//@Repository
public interface FraudTrackerRepository {
    @Query(value = "SELECT * FROM m_fraud_tracker WHERE deleted = false AND id=?1", nativeQuery = true)
    List<FraudTracker> findByFraudTrackerId(Long id);

    @Query(value = "SELECT * FROM m_fraud_tracker WHERE deleted = false", nativeQuery = true)
    Page<FraudTracker> findAllFraudEvents(Pageable pageable);

    //SECOND_RULE
    @Query(value = "SELECT * FROM m_fraud_tracker WHERE deleted = false AND ip_address=?1 AND violated=false AND COUNT(*)=3 " +
            "ORDER BY date_created DESC", nativeQuery = true)
    List<FraudTracker> getByIpAddressAndTransactionCountIsThree(String ipAddress);

    //FOURTH_RULE
    @Query(value = "SELECT * FROM m_fraud_tracker WHERE deleted = false AND device_signature=?1 AND violated=false AND COUNT(*)=3 " +
            "ORDER BY date_created DESC", nativeQuery = true)
    List<FraudTracker> getByEmailAddressAndTransactionCountIsThree(String deviceSignature);

    //FIFTH_RULE
    @Query(value = "SELECT * FROM m_fraud_tracker WHERE deleted = false AND hashed_pan=?1 AND violated=false AND COUNT(*)=3 " +
            "ORDER BY date_created DESC", nativeQuery = true)
    List<FraudTracker> getByHashedPanAndTransactionCountIsThree(String hashedPan);

    //SIXTH_RULE
    @Query(value = "SELECT * FROM m_fraud_tracker WHERE deleted = false AND device_signature=?1 AND violated=false AND COUNT(*)=3 " +
            "ORDER BY date_created DESC", nativeQuery = true)
    List<FraudTracker> getByDeviceSignatureAndTransactionCountIsThree(String deviceSignature);
}
