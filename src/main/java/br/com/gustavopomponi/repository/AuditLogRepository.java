package br.com.gustavopomponi.repository;

import br.com.gustavopomponi.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<AuditLog> findByResourceTypeAndResourceId(String resourceType, String resourceId);
}