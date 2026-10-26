package com.deluxeterno.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class ProductForm {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 120, message = "El nombre no debe superar 120 caracteres.")
    private String name;

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 1000, message = "La descripción no debe superar 1000 caracteres.")
    private String description;

    @NotBlank(message = "La imagen es obligatoria.")
    @Size(max = 255, message = "La ruta de imagen no debe superar 255 caracteres.")
    @Pattern(regexp = "^(?:/(?!/)[^\\s\\\\]*|https?://[^\\s\\\\]+)$",
            message = "Usa una ruta local que comience con / o una URL http(s) válida.")
    private String imagePath;

    @NotNull(message = "La categoría es obligatoria.")
    private Long categoryId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
