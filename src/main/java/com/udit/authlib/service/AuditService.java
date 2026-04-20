package com.udit.authlib.service;

import com.udit.authlib.entity.AuditLog;
import com.udit.authlib.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(String username, String action, String ipAddress, String details) {
        log.info("Audit Log: User={}, Action={}, IP={}, Details={}", username, action, ipAddress, details);
        
        AuditLog auditLog = AuditLog.builder()
                .username(username)
                .action(action)
                .ipAddress(ipAddress)
                .details(details)
                .build();
                
        auditLogRepository.save(auditLog);
    }
}
