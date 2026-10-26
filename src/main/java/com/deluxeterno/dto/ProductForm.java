package com.deluxeterno.dto;

import com.deluxeterno.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductForm {
    @NotBlank(message="El nombre es obligatorio.") @Size(max=120)
    private String name = "";
    @Size(max=1000)
    private String description = "";
    @Size(max=255) @Pattern(regexp="^(?:$|/(?!/)[^\\s\\\\]*|https?://[^\\s\\\\]+)$", message="Usa una ruta local o una URL http(s) válida.")
    private String imagePath = "";
    @NotNull(message="Selecciona una categoría.")
    private Long categoryId = null;
    @NotNull
    private PublicationStatus publicationStatus = PublicationStatus.PUBLISHED;
    @NotNull
    private SupplyMode supplyMode = SupplyMode.MADE_TO_ORDER;
    @NotNull
    private PriceMode priceMode = PriceMode.CONSULT;
    @DecimalMin(value="0.01",message="El precio debe ser mayor que cero.") @Digits(integer=8,fraction=2)
    private BigDecimal price = null;
    @Size(max=1000)
    private String includedItems = "";
    @Size(max=500)
    private String personalization = "";
    @Size(max=160)
    private String occasions = "";
    @Size(max=120)
    private String preparationTime = "";
    @Size(max=1000)
    private String internalNotes = "";
    private boolean temporarilyUnavailable = false;
    @Min(0) @Max(1000000)
    private int lowStockThreshold = 3;
    private Long version = null;
    private boolean gallerySubmitted = false;
    @Min(0) @Max(100)
    private int focusX = 50;
    @Min(0) @Max(100)
    private int focusY = 50;
    @Size(max=4,message="Se permiten hasta cinco fotografías en total.")
    private List<@Size(max=255) @Pattern(regexp="^(?:/(?!/)[^\\s\\\\]*|https?://[^\\s\\\\]+)$") String> additionalImagePaths = new ArrayList<>();
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String value) { imagePath = value; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long value) { categoryId = value; }
    public PublicationStatus getPublicationStatus() { return publicationStatus; }
    public void setPublicationStatus(PublicationStatus value) { publicationStatus = value; }
    public SupplyMode getSupplyMode() { return supplyMode; }
    public void setSupplyMode(SupplyMode value) { supplyMode = value; }
    public PriceMode getPriceMode() { return priceMode; }
    public void setPriceMode(PriceMode value) { priceMode = value; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal value) { price = value; }
    public String getIncludedItems() { return includedItems; }
    public void setIncludedItems(String value) { includedItems = value; }
    public String getPersonalization() { return personalization; }
    public void setPersonalization(String value) { personalization = value; }
    public String getOccasions() { return occasions; }
    public void setOccasions(String value) { occasions = value; }
    public String getPreparationTime() { return preparationTime; }
    public void setPreparationTime(String value) { preparationTime = value; }
    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String value) { internalNotes = value; }
    public boolean isTemporarilyUnavailable() { return temporarilyUnavailable; }
    public void setTemporarilyUnavailable(boolean value) { temporarilyUnavailable = value; }
    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int value) { lowStockThreshold = value; }
    public Long getVersion() { return version; }
    public void setVersion(Long value) { version = value; }
    public boolean isGallerySubmitted() { return gallerySubmitted; }
    public void setGallerySubmitted(boolean value) { gallerySubmitted = value; }
    public int getFocusX() { return focusX; }
    public void setFocusX(int value) { focusX = value; }
    public int getFocusY() { return focusY; }
    public void setFocusY(int value) { focusY = value; }
    public List<@Size(max=255) @Pattern(regexp="^(?:/(?!/)[^\\s\\\\]*|https?://[^\\s\\\\]+)$") String> getAdditionalImagePaths() { return additionalImagePaths; }
    public void setAdditionalImagePaths(List<@Size(max=255) @Pattern(regexp="^(?:/(?!/)[^\\s\\\\]*|https?://[^\\s\\\\]+)$") String> value) { additionalImagePaths = value; }

    @AssertTrue(message="Para publicar, añade una descripción y una fotografía principal.")
    public boolean isReadyToPublish() {
        return publicationStatus != PublicationStatus.PUBLISHED ||
            (description != null && !description.isBlank() && imagePath != null && !imagePath.isBlank());
    }
    @AssertTrue(message="Indica el importe cuando el precio sea fijo o desde.")
    public boolean isPriceConsistent() { return priceMode == PriceMode.CONSULT || price != null; }
}
