package br.com.gustavopomponi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardUploadResult {

    private Integer lineNumber;
    private String status;
    private String maskedPan;
    private String errorMessage;
    private UUID cardId;
}