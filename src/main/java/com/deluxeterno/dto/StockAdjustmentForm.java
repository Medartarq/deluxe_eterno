package com.deluxeterno.dto;

import com.deluxeterno.domain.StockMovementType;
import jakarta.validation.constraints.*;

public class StockAdjustmentForm {
    @NotNull private StockMovementType type;
    @Min(0) @Max(1000000) private int quantity;
    @NotBlank(message="Indica el motivo del movimiento.") @Size(max=255) private String reason;
    @NotNull private Long version;
    public StockMovementType getType() { return type; }
    public void setType(StockMovementType value) { type=value; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int value) { quantity=value; }
    public String getReason() { return reason; }
    public void setReason(String value) { reason=value; }
    public Long getVersion() { return version; }
    public void setVersion(Long value) { version=value; }
}
