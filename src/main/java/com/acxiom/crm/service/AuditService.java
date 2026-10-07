package com.acxiom.crm.service;

import com.acxiom.crm.model.AuditLog;
import com.acxiom.crm.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Transactional
    public void log(String action, String entityName, String recordId, Object oldObject, Object newObject) {
        String userId = getCurrentUsername();
        String ipAddress = getCurrentClientIp();
        String oldJson = toJson(oldObject);
        String newJson = toJson(newObject);

        AuditLog log = new AuditLog(userId, action, entityName, recordId, oldJson, newJson, ipAddress);
        auditLogRepository.save(log);
    }

    @Transactional
    public void logEvent(String userId, String action, String entityName, String recordId, String details, String ipAddress) {
        AuditLog log = new AuditLog(userId != null ? userId : getCurrentUsername(),
                action, entityName, recordId, null, details,
                ipAddress != null ? ipAddress : getCurrentClientIp());
        auditLogRepository.save(log);
    }

    public Page<AuditLog> getLogs(String userId, String entityName, String action,
                                  LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return auditLogRepository.searchAuditLogs(userId, entityName, action, start, end, pageable);
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) return (String) obj;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    public String getCurrentClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip != null ? ip : "127.0.0.1";
            }
        } catch (Exception ignored) {
        }
        return "127.0.0.1";
    }
}
