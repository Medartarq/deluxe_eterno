package com.deluxeterno.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Version;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false, length = 255)
    private String imagePath;

    @Column(length = 255)
    private String secondaryImagePath;

    @Column(unique = true, length = 40)
    private String catalogKey;

    @Column(unique = true, length = 40)
    private String sku;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private PublicationStatus publicationStatus;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private SupplyMode supplyMode;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private PriceMode priceMode;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 1000)
    private String includedItems;

    @Column(length = 500)
    private String personalization;

    @Column(length = 160)
    private String occasions;

    @Column(length = 120)
    private String preparationTime;

    @Column(length = 1000)
    private String internalNotes;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean temporarilyUnavailable;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int stockQuantity;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int reservedQuantity;

    @Column(nullable = false, columnDefinition = "integer default 3")
    private int lowStockThreshold = 3;

    @Column(nullable = false, columnDefinition = "integer default 50") private int focusX = 50;
    @Column(nullable = false, columnDefinition = "integer default 50") private int focusY = 50;

    @Version @Column(nullable = false, columnDefinition = "bigint default 0")
    private long version;

    private Instant updatedAt;

    @Column(length = 80)
    private String updatedBy;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @OrderColumn(name = "image_order")
    @Column(name = "image_path", length = 255, nullable = false)
    private List<String> additionalImagePaths = new ArrayList<>();

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    protected Product() {
    }

    public Product(String name, String description, String imagePath, Category category) {
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

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

    public boolean isActive() {
        return active;
    }

    public String getSecondaryImagePath() { return secondaryImagePath; }

    public void setSecondaryImagePath(String secondaryImagePath) { this.secondaryImagePath = secondaryImagePath; }

    public String getCatalogKey() { return catalogKey; }

    public void setCatalogKey(String catalogKey) { this.catalogKey = catalogKey; }

    public void setActive(boolean active) {
        this.active = active;
        this.publicationStatus = active ? PublicationStatus.PUBLISHED : PublicationStatus.ARCHIVED;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getSku() { return sku != null ? sku : (catalogKey != null ? catalogKey : "DE-" + id); }
    public void setSku(String value) { sku = value; }
    public PublicationStatus getPublicationStatus() { return publicationStatus != null ? publicationStatus : (active ? PublicationStatus.PUBLISHED : PublicationStatus.ARCHIVED); }
    public void setPublicationStatus(PublicationStatus value) { publicationStatus = value; active = value == PublicationStatus.PUBLISHED; }
    public SupplyMode getSupplyMode() { return supplyMode == null ? SupplyMode.MADE_TO_ORDER : supplyMode; }
    public void setSupplyMode(SupplyMode value) { supplyMode = value; }
    public PriceMode getPriceMode() { return priceMode == null ? PriceMode.CONSULT : priceMode; }
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
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int value) { stockQuantity = value; }
    public int getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(int value) { reservedQuantity = value; }
    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int value) { lowStockThreshold = value; }
    public int getAvailableQuantity() { return stockQuantity - reservedQuantity; }
    public boolean isLowStock() { return getSupplyMode() == SupplyMode.STOCK && getAvailableQuantity() <= lowStockThreshold; }
    public long getVersion() { return version; }
    public int getFocusX() { return focusX; }
    public void setFocusX(int value) { focusX=value; }
    public int getFocusY() { return focusY; }
    public void setFocusY(int value) { focusY=value; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String value) { updatedBy = value; }
    public List<String> getAdditionalImagePaths() { return additionalImagePaths; }
    public List<String> getGalleryImages() {
        var images = new LinkedHashSet<String>();
        if (imagePath != null && !imagePath.isBlank()) images.add(imagePath);
        if (secondaryImagePath != null && !secondaryImagePath.isBlank()) images.add(secondaryImagePath);
        images.addAll(additionalImagePaths);
        return new ArrayList<>(images);
    }
    public String getAvailabilityLabel() {
        if (temporarilyUnavailable) return "Temporalmente no disponible";
        if (getSupplyMode() == SupplyMode.MADE_TO_ORDER) return "Por encargo";
        return getAvailableQuantity() <= 0 ? "Agotado" : (isLowStock() ? "Últimas unidades" : "Disponible");
    }
    public String getPriceLabel() {
        if (getPriceMode() == PriceMode.CONSULT || price == null) return "Precio a consultar";
        return (getPriceMode() == PriceMode.FROM ? "Desde " : "") + String.format(Locale.forLanguageTag("es-PE"), "S/ %.2f", price);
    }
    @PrePersist @PreUpdate
    void touch() { updatedAt = Instant.now(); }
}
