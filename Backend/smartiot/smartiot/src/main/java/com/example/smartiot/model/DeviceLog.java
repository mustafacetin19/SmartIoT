package com.example.smartiot.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "device_logs")
public class DeviceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "ts", nullable = false, updatable = false)
    private LocalDateTime ts;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(length = 32)
    private String action;   // ON/OFF/SET_LEVEL vb.

    @Column(length = 16)
    private String result;   // SUCCESS | FAIL

    @Column(columnDefinition = "text")
    private String message;

    // JSONB eşlemesi
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta", columnDefinition = "jsonb")
    private JsonNode meta;   // opsiyonel; null bırakılabilir
}
