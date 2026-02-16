package br.com.gustavopomponi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO - Only returns masked PAN
 * PCI DSS Requirement 3.3: Mask PAN when displayed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardResponse {

    private UUID id;

    @JsonIgnore
    private String maskedPan;
    @JsonIgnore
    private Boolean isActive;
    @JsonIgnore
    private LocalDateTime createdAt;
    @JsonIgnore
    private LocalDateTime lastUsedAt;

}