package com.deluxeterno.domain;

public enum PriceMode {
    CONSULT("Consultar"), FIXED("Precio fijo"), FROM("Desde");
    private final String label;
    PriceMode(String label) { this.label = label; }
    public String getLabel() { return label; }
}
