package br.com.gustavopomponi.service;

import br.com.gustavopomponi.dto.CreditCardRequest;
import br.com.gustavopomponi.dto.CreditCardResponse;
import br.com.gustavopomponi.entity.CreditCard;
import br.com.gustavopomponi.repository.CreditCardRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class CreditCardService {

    private static final Logger logger = LoggerFactory.getLogger(CreditCardService.class);

    private final CreditCardRepository creditCardRepository;
    private final EncryptionService encryptionService;
    private final AuditLogService auditLogService;

    public CreditCardService(CreditCardRepository creditCardRepository,
                             EncryptionService encryptionService,
                             AuditLogService auditLogService) {
        this.creditCardRepository = creditCardRepository;
        this.encryptionService = encryptionService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public CreditCardResponse saveCard(Long userId, CreditCardRequest request, String ipAddress) throws Exception {


        String cleanCardNumber = request.getCardNumber().replaceAll("[\\s-]", "");

        CreditCard card = new CreditCard();
        card.setUserId(userId);

        card.setFirstSixDigits(cleanCardNumber.substring(0, 6));
        card.setLastFourDigits(cleanCardNumber.substring(cleanCardNumber.length() - 4));

        String encryptedPan = encryptionService.encryptPAN(cleanCardNumber);
        card.setEncryptedPan(encryptedPan);

        card.setIsActive(true);

        CreditCard savedCard = creditCardRepository.save(card);
        auditLogService.logCardCreation(userId, savedCard.getLastFourDigits(), ipAddress);

        logger.info("Credit card saved successfully for user: {}", userId);

        return mapToResponse(savedCard);
    }


    public Optional<CreditCardResponse> findCardByNumber(Long userId, String cardNumber, String ipAddress) {

        String cleanCardNumber = cardNumber.replaceAll("[\\s-]", "");

        String maskedSearchPan = encryptionService.maskPAN(cleanCardNumber);
        auditLogService.logCardAccess(userId, "SEARCH_CARD_BY_PAN", maskedSearchPan, ipAddress);

        logger.info("Searching for card {} for user {}", maskedSearchPan, userId);

        try {

            List<CreditCard> userCards = creditCardRepository.findByUserIdAndIsActive(userId, true);


            for (CreditCard card : userCards) {
                try {
                    String decryptedPan = encryptionService.decryptPAN(card.getEncryptedPan());

                    if (decryptedPan.equals(cleanCardNumber)) {
                        logger.info("Card found: {} for user {}", maskedSearchPan, userId);
                        auditLogService.logCardAccess(userId, "SEARCH_CARD_FOUND",
                                card.getId().toString(), ipAddress);
                        return Optional.of(mapToResponse(card));
                    }
                } catch (Exception e) {
                    logger.error("Failed to decrypt card {} during search", card.getId(), e);
                    // Continue searching other cards
                }
            }

            logger.info("Card not found: {} for user {}", maskedSearchPan, userId);
            auditLogService.logCardAccess(userId, "SEARCH_CARD_NOT_FOUND", maskedSearchPan, ipAddress);
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Error searching for card", e);
            auditLogService.logCardAccess(userId, "SEARCH_CARD_ERROR",
                    e.getMessage(), ipAddress);
            return Optional.empty();
        }
    }


    private CreditCardResponse mapToResponse(CreditCard card) {

        String maskedPan = card.getFirstSixDigits() +
                "******" +
                card.getLastFourDigits();

        return CreditCardResponse.builder()
                .id(card.getId())
                .maskedPan(maskedPan)
                .isActive(card.getIsActive())
                .createdAt(card.getCreatedAt())
                .lastUsedAt(card.getLastUsedAt())
                .build();
    }


}