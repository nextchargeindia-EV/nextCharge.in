package com.nextcharge.api.service;

import com.nextcharge.api.model.AuditLog;
import com.nextcharge.api.model.User;
import com.nextcharge.api.repository.AuditLogRepository;
import com.nextcharge.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void log(UUID userId, String action, String details, String ipAddress) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        AuditLog log = AuditLog.builder()
                .user(user)
                .action(action)
                .details(details)
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsForUser(UUID userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }
}
