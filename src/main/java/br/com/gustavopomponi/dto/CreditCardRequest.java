package br.com.gustavopomponi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;


@Data
public class CreditCardRequest {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Card number must be 13-19 digits")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String cardNumber;

}