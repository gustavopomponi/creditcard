package br.com.gustavopomponi.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "credit_cards", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_last_four", columnList = "last_four_digits")
})
@Data
public class CreditCard {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // PCI DSS Requirement 3.3: Mask PAN when displayed
    // Store first 6 (BIN) and last 4 for display purposes
    @Column(name = "first_six_digits", length = 6, nullable = false)
    private String firstSixDigits;

    @Column(name = "last_four_digits", length = 4, nullable = false)
    private String lastFourDigits;

    // PCI DSS Requirement 3.4: Render PAN unreadable
    // Full PAN encrypted with AES-256-GCM
    @Column(name = "encrypted_pan", nullable = false, columnDefinition = "TEXT")
    private String encryptedPan;

    // PCI DSS Requirement 3.1: Keep cardholder data storage to a minimum
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Track when card was last used
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}