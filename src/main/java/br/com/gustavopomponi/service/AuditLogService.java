package br.com.gustavopomponi.service;

import br.com.gustavopomponi.entity.AuditLog;
import br.com.gustavopomponi.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogService {


    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logCardAccess(Long userId, String action, String cardId, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setResourceType("CREDIT_CARD");
        log.setResourceId(cardId);
        log.setIpAddress(ipAddress);
        log.setTimestamp(LocalDateTime.now());

        auditLogRepository.save(log);
    }

    public void logCardCreation(Long userId, String lastFour, String ipAddress) {
        logCardAccess(userId, "CREATE_CARD", "****" + lastFour, ipAddress);
    }

    public void logCardRetrieval(Long userId, String cardId, String ipAddress) {
        logCardAccess(userId, "VIEW_CARD", cardId, ipAddress);
    }

}