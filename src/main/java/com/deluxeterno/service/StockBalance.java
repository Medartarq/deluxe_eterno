package com.deluxeterno.service;

import com.deluxeterno.domain.StockMovementType;

/** Cantidades físicas y reservas se mantienen separadas; una salida no libera reservas implícitamente. */
public record StockBalance(int physical, int reserved) {
    public StockBalance {
        if (physical < 0 || reserved < 0 || reserved > physical || physical > 1000000)
            throw new IllegalArgumentException("El saldo físico no puede ser negativo ni menor que las reservas.");
    }
    public StockBalance apply(StockMovementType type, int quantity) {
        if (type == null || quantity < 0 || quantity > 1000000 || (quantity == 0 && type != StockMovementType.CORRECTION))
            throw new IllegalArgumentException("Indica una cantidad mayor que cero; solo una corrección admite cero.");
        return switch (type) {
            case IN -> new StockBalance(Math.addExact(physical,quantity),reserved);
            case OUT -> new StockBalance(physical-quantity,reserved);
            case RESERVE -> new StockBalance(physical,Math.addExact(reserved,quantity));
            case RELEASE -> new StockBalance(physical,reserved-quantity);
            case CORRECTION -> new StockBalance(quantity,reserved);
        };
    }
}
