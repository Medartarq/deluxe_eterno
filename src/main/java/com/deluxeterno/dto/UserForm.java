package com.deluxeterno.dto;

import jakarta.validation.constraints.*;
import java.nio.charset.StandardCharsets;

public class UserForm {
    @NotBlank(message = "Indica el nombre de usuario.")
    @Size(min = 3, max = 80, message = "El usuario debe tener entre 3 y 80 caracteres.")
    @Pattern(regexp = "[A-Za-z0-9._-]+", message = "Usa letras sin tildes, números, puntos, guiones o guiones bajos.")
    private String username = "";

    @NotBlank(message = "Indica una contraseña.")
    @Size(min = 12, max = 72, message = "La contraseña debe tener entre 12 y 72 caracteres.")
    private String password = "";

    @NotBlank(message = "Confirma la contraseña.")
    @Size(max = 72, message = "La confirmación no debe superar 72 caracteres.")
    private String passwordConfirmation = "";

    public String getUsername() { return username; }
    public void setUsername(String value) { username = value; }
    public String getPassword() { return password; }
    public void setPassword(String value) { password = value; }
    public String getPasswordConfirmation() { return passwordConfirmation; }
    public void setPasswordConfirmation(String value) { passwordConfirmation = value; }

    @AssertTrue(message = "Las contraseñas deben coincidir.")
    public boolean isPasswordConfirmed() {
        return password == null || password.isEmpty() || password.equals(passwordConfirmation);
    }

    @AssertTrue(message = "La contraseña supera el límite de 72 bytes. Reduce su longitud, especialmente si usas emojis o caracteres especiales.")
    public boolean isPasswordWithinByteLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
