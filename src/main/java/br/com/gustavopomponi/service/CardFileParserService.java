package br.com.gustavopomponi.service;

import br.com.gustavopomponi.dto.CreditCardRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CardFileParserService {

    public List<CreditCardRequest> parseFile(MultipartFile file) throws Exception {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        long maxFileSize = 10 * 1024 * 1024;
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed (10 MB)");
        }

        return parseTXT(file);

    }


    private List<CreditCardRequest> parseTXT(MultipartFile file) throws Exception {

        List<CreditCardRequest> cards = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                if (line.trim().startsWith("#") || line.trim().startsWith("//") || !line.trim().startsWith("C")) {
                    continue;
                }

                try {
                    CreditCardRequest card = parseTXTLine(line, lineNumber);
                    cards.add(card);
                } catch (Exception e) {
                    throw new Exception("Error parsing line " + lineNumber + ": " + e.getMessage(), e);
                }
            }
        }

        return cards;
    }


    private CreditCardRequest parseTXTLine(String line, int lineNumber) {

        String trim = line.substring(7, line.length() - 1).trim();
        String substringCard = trim.length() > 19 ? trim.substring(0, 19).trim() : trim;

        CreditCardRequest request = new CreditCardRequest();

        try {
            request.setCardNumber(substringCard);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format at line " + lineNumber + ": " + e.getMessage());
        }

        return request;
    }

}