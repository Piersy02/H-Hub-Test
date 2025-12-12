package com.ids.hhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class LoginRequestDto {

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "Il formato dell'email non è valido")
    private String email;

    @NotBlank(message = "La password è obbligatoria")
    private String password;
}
