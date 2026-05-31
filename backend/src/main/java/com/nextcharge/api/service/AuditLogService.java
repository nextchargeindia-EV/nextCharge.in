package com.nextcharge.api.service;

import com.nextcharge.api.model.AuditLog;
import java.util.List;
import java.util.UUID;

public interface AuditLogService {
    void log(UUID userId, String action, String details, String ipAddress);
    List<AuditLog> getLogsForUser(UUID userId);
    List<AuditLog> getAllLogs();
}
