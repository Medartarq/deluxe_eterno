package com.deluxeterno.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InquiryForm {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 120, message = "El nombre no debe superar 120 caracteres.")
    private String name;

    @Email(message = "Ingresa un correo válido.")
    @NotBlank(message = "El correo es obligatorio.")
    @Size(max = 180, message = "El correo no debe superar 180 caracteres.")
    private String email;

    @Size(max = 40, message = "El teléfono no debe superar 40 caracteres.")
    private String phone;

    @NotBlank(message = "El mensaje es obligatorio.")
    @Size(max = 2000, message = "El mensaje no debe superar 2000 caracteres.")
    private String message;

    private String website;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
