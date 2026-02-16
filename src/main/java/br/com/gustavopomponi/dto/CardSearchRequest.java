package br.com.gustavopomponi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CardSearchRequest {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9\\s-]{13,19}$", message = "Invalid card number format")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String cardNumber;
}