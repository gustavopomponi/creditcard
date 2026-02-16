package br.com.gustavopomponi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchUploadResponse {

    private Integer totalRecords;
    private Integer successCount;
    private Integer failureCount;

    @Builder.Default
    private List<CardUploadResult> results = new ArrayList<>();

    private Long processingTimeMs;
    private String message;
}