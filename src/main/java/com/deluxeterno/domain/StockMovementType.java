package com.deluxeterno.domain;

public enum StockMovementType {
    IN("Entrada"), OUT("Salida"), RESERVE("Reserva"), RELEASE("Liberación"), CORRECTION("Corrección de existencia física");
    private final String label;
    StockMovementType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
