package br.com.gustavopomponi.service;

import br.com.gustavopomponi.dto.BatchUploadResponse;
import br.com.gustavopomponi.dto.CardUploadResult;
import br.com.gustavopomponi.dto.CreditCardRequest;
import br.com.gustavopomponi.dto.CreditCardResponse;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class BatchCardUploadService {

    private static final Logger logger = LoggerFactory.getLogger(BatchCardUploadService.class);


    private final CardFileParserService fileParserService;
    private final CreditCardService creditCardService;
    private final AuditLogService auditLogService;

    public BatchCardUploadService(CardFileParserService fileParserService,
                                  CreditCardService creditCardService,
                                  AuditLogService auditLogService) {
        this.fileParserService = fileParserService;
        this.creditCardService = creditCardService;
        this.auditLogService = auditLogService;
    }


    @Transactional
    public BatchUploadResponse processBatchUpload(
            Long userId,
            MultipartFile file,
            String fileType,
            String ipAddress) {

        long startTime = System.currentTimeMillis();

        logger.info("Starting batch upload for user {} from IP {}", userId, ipAddress);
        auditLogService.logCardAccess(userId, "BATCH_UPLOAD_START",
                file.getOriginalFilename(), ipAddress);

        BatchUploadResponse response = BatchUploadResponse.builder()
                .results(new ArrayList<>())
                .successCount(0)
                .failureCount(0)
                .build();

        try {

            List<CreditCardRequest> cards = fileParserService.parseFile(file);
            response.setTotalRecords(cards.size());

            if (cards.isEmpty()) {
                response.setMessage("No cards found in file");
                return response;
            }

            logger.info("Parsed {} cards from file", cards.size());

            int lineNumber = 1;
            for (CreditCardRequest cardRequest : cards) {
                CardUploadResult result = processCard(userId, cardRequest, lineNumber, ipAddress);
                response.getResults().add(result);

                if ("SUCCESS".equals(result.getStatus())) {
                    response.setSuccessCount(response.getSuccessCount() + 1);
                } else {
                    response.setFailureCount(response.getFailureCount() + 1);
                }

                lineNumber++;
            }

            long endTime = System.currentTimeMillis();
            response.setProcessingTimeMs(endTime - startTime);

            response.setMessage(String.format(
                    "Processed %d cards: %d successful, %d failed",
                    response.getTotalRecords(),
                    response.getSuccessCount(),
                    response.getFailureCount()
            ));

            auditLogService.logCardAccess(userId, "BATCH_UPLOAD_COMPLETE",
                    String.format("Success: %d, Failed: %d", response.getSuccessCount(), response.getFailureCount()),
                    ipAddress);

            logger.info("Batch upload completed for user {}: {} successful, {} failed",
                    userId, response.getSuccessCount(), response.getFailureCount());

        } catch (Exception e) {
            logger.error("Batch upload failed for user {}", userId, e);
            response.setMessage("Failed to process file: " + e.getMessage());
            response.setFailureCount(response.getTotalRecords() != null ? response.getTotalRecords() : 0);

            auditLogService.logCardAccess(userId, "BATCH_UPLOAD_FAILED",
                    e.getMessage(), ipAddress);
        }

        return response;
    }

    private CardUploadResult processCard(
            Long userId,
            CreditCardRequest cardRequest,
            int lineNumber,
            String ipAddress) {

        try {
            CreditCardResponse savedCard = creditCardService.saveCard(userId, cardRequest, ipAddress);

            return CardUploadResult.builder()
                    .lineNumber(lineNumber)
                    .status("SUCCESS")
                    .maskedPan(savedCard.getMaskedPan())
                    .cardId(savedCard.getId())
                    .build();

        } catch (IllegalArgumentException e) {
            logger.warn("Validation failed for card at line {}: {}", lineNumber, e.getMessage());

            return CardUploadResult.builder()
                    .lineNumber(lineNumber)
                    .status("FAILURE")
                    .errorMessage(e.getMessage())
                    .build();

        } catch (Exception e) {
            logger.error("Failed to save card at line {}", lineNumber, e);

            return CardUploadResult.builder()
                    .lineNumber(lineNumber)
                    .status("FAILURE")
                    .errorMessage("Failed to save card: " + e.getMessage())
                    .build();
        }
    }
}