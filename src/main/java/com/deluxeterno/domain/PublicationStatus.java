package com.deluxeterno.domain;

public enum PublicationStatus {
    DRAFT("Borrador"), PUBLISHED("Publicado"), ARCHIVED("Archivado");
    private final String label;
    PublicationStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
