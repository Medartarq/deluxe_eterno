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
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
