package com.wayapaychat.paymentgateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Shedlock {

    @Id
    private String name;
    private Timestamp lockUntil;
    private Timestamp lockedAt;
    private String lockedBy;

}
