package com.wayapaychat.paymentgateway.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "m_processor_configuration")
public class ProcessorConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    @JsonIgnore
    private Long id;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

//    @Column(name = "test_base_url")
//    private String testBaseUrl;

//    @Column(name = "live_base_url")
//    private String liveBaseUrl;

    @Column(name = "card_acquiring")
    private Boolean cardAcquiring;

    @Column(name = "ussd_acquiring")
    private Boolean ussdAcquiring;

    @Column(name = "account_acquiring")
    private Boolean accountAcquiring;

    @Column(name = "payattitude_acquiring")
    private Boolean payattitudeAcquiring;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dateCreated;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dateModified;

    @Column(nullable = false, name = "created_by")
    private Long createdBy;

    @Column(name = "modified_by")
    private Long modifiedBy;

    @Column(name = "deleted")
    private Boolean deleted;

    @PrePersist
    void onCreate() {
        if (ObjectUtils.isEmpty(dateCreated))
            this.dateCreated = LocalDateTime.now();
        if (ObjectUtils.isEmpty(deleted))
            deleted = false;
        if (ObjectUtils.isEmpty(createdBy))
            createdBy = 0L;
    }

    @PreUpdate
    void onUpdate() {
        if (ObjectUtils.isEmpty(dateModified))
            this.dateModified = LocalDateTime.now();
        if (ObjectUtils.isEmpty(modifiedBy))
            modifiedBy = 0L;
    }
}
