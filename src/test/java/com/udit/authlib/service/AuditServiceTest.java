package com.udit.authlib.service;

import com.udit.authlib.entity.AuditLog;
import com.udit.authlib.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    void logPersistsAuditLogDetails() {
        auditService.log("testuser", "LOGIN_SUCCESS", "127.0.0.1", "User authenticated successfully");

        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog auditLog = auditLogCaptor.getValue();
        assertThat(auditLog.getUsername()).isEqualTo("testuser");
        assertThat(auditLog.getAction()).isEqualTo("LOGIN_SUCCESS");
        assertThat(auditLog.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(auditLog.getDetails()).isEqualTo("User authenticated successfully");
    }
}
