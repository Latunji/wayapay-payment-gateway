package com.wayapaychat.paymentgateway.entity;

import com.wayapaychat.paymentgateway.entity.listener.FraudEventEntityListener;
import com.wayapaychat.paymentgateway.enumm.SettlementStatus;
import com.wayapaychat.paymentgateway.enumm.WithdrawalStatus;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@EntityListeners(value = FraudEventEntityListener.class)
@Builder
@AllArgsConstructor
@Entity
@Table(name = "m_withdrawal", indexes =
        {
                @Index(name = "ix_tbl_mts__col_withdrawal_reference_id__uq", columnList = "withdrawal_reference_id", unique = true)
        }
)
public class Withdrawals extends GenericBaseEntity {

        @Column(nullable = false, columnDefinition = "VARCHAR(255)")
        private String merchantId;

        @Column(name = "merchant_user_id")
        private Long merchantUserId;

        @Enumerated(EnumType.STRING)
        @Column(name = "amount")
        private BigDecimal amount;

        @Column(name = "withdrawal_reference_id", columnDefinition = "VARCHAR(255)")
        private String withdrawalReferenceId;

        @Column(name = "withdrawal_date")
        private LocalDateTime withdrawalDate = LocalDateTime.now();

        @Column(name = "withdrawal_status")
        private WithdrawalStatus withdrawalStatus;
}
