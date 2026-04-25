package com.udit.authlib.entity;

import com.udit.authlib.entity.base.BaseIdEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog extends BaseIdEntity {

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String action;

    private String ipAddress;

    @CreationTimestamp
    @Column(name = "timestamp", updatable = false)
    private LocalDateTime timestamp;

    @Column(length = 1000)
    private String details;
}
