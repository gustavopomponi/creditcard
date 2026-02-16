package br.com.gustavopomponi.controller;

import br.com.gustavopomponi.dto.BatchUploadResponse;
import br.com.gustavopomponi.dto.CardSearchRequest;
import br.com.gustavopomponi.dto.CreditCardRequest;
import br.com.gustavopomponi.dto.CreditCardResponse;
import br.com.gustavopomponi.service.BatchCardUploadService;
import br.com.gustavopomponi.service.CreditCardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/credit-cards")
public class CreditCardController {

    private static final Logger logger = LoggerFactory.getLogger(CreditCardController.class);

    private final CreditCardService creditCardService;
    private final BatchCardUploadService batchCardUploadService;

    public CreditCardController(CreditCardService creditCardService, BatchCardUploadService batchCardUploadService) {
        this.creditCardService = creditCardService;
        this.batchCardUploadService = batchCardUploadService;
    }

    @PostMapping
    public ResponseEntity<?> addCreditCard(
            @Valid @RequestBody CreditCardRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getUserIdFromAuth(authentication);
            String ipAddress = getClientIpAddress(httpRequest);

            CreditCardResponse response = creditCardService.saveCard(userId, request, ipAddress);

            logger.info("Card added successfully for user: {}", userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid card data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to add card", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process card"));
        }
    }

    @PostMapping(value = "/batch-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> batchUploadCards(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileType") String fileType,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getUserIdFromAuth(authentication);
            String ipAddress = getClientIpAddress(httpRequest);

            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "File is empty"));
            }

            String filename = file.getOriginalFilename();
            if (filename != null && !filename.toLowerCase().endsWith("." + fileType.toLowerCase())) {
                logger.warn("File extension mismatch: {} for type {}", filename, fileType);
            }

            logger.info("Batch upload started by user {} with file {} ({})",
                    userId, filename, fileType);

            BatchUploadResponse response = batchCardUploadService.processBatchUpload(
                    userId, file, fileType, ipAddress);

            HttpStatus status = response.getFailureCount() > 0 && response.getSuccessCount() == 0
                    ? HttpStatus.BAD_REQUEST
                    : HttpStatus.OK;

            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            logger.error("Batch upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Batch upload failed: " + e.getMessage()));
        }
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchCardByNumber(
            @Valid @RequestBody CardSearchRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        try {
            Long userId = getUserIdFromAuth(authentication);
            String ipAddress = getClientIpAddress(httpRequest);

            logger.info("Card search initiated by user {} from IP {}", userId, ipAddress);

            Optional<CreditCardResponse> card = creditCardService.findCardByNumber(
                    userId, request.getCardNumber(), ipAddress);

            if (card.isPresent()) {
                return ResponseEntity.ok(card.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Card not found",
                                "message", "No card found with the provided number"
                        ));
            }

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid search request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid card number format"));
        } catch (Exception e) {
            logger.error("Card search failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Search failed"));
        }
    }


    private Long getUserIdFromAuth(Authentication authentication) {
        // TODO: Extract user ID from JWT token
        // For now, returning mock value
        return 1L;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}