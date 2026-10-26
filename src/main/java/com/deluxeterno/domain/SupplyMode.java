package com.deluxeterno.domain;

public enum SupplyMode {
    MADE_TO_ORDER("Por encargo"), STOCK("Por unidades");
    private final String label;
    SupplyMode(String label) { this.label = label; }
    public String getLabel() { return label; }
}
