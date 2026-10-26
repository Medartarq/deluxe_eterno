package com.deluxeterno.service;

import com.deluxeterno.domain.*;
import com.deluxeterno.dto.StockAdjustmentForm;
import com.deluxeterno.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventoryService {
    private final ProductRepository products;
    private final StockMovementRepository movements;
    public InventoryService(ProductRepository products, StockMovementRepository movements) { this.products=products; this.movements=movements; }

    @Transactional
    public void adjust(Long id, StockAdjustmentForm form, String actor) {
        var product=products.findForUpdate(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (product.getSupplyMode()!=SupplyMode.STOCK) throw new IllegalArgumentException("Este producto se prepara por encargo y no controla unidades.");
        if (form.getVersion()==null || form.getVersion()!=product.getVersion())
            throw new IllegalArgumentException("El saldo cambió mientras editabas. Revisa el saldo actualizado antes de volver a enviar.");
        if (form.getReason()==null || form.getReason().isBlank() || form.getReason().length()>255)
            throw new IllegalArgumentException("Indica un motivo de hasta 255 caracteres.");
        int previous=product.getStockQuantity(), previousReserved=product.getReservedQuantity();
        var result=new StockBalance(previous,previousReserved).apply(form.getType(),form.getQuantity());
        product.setStockQuantity(result.physical()); product.setReservedQuantity(result.reserved()); product.setUpdatedBy(actor);
        // Un ajuste que no cambia cantidades no debe crear entradas engañosas en el historial.
        if (previous==result.physical() && previousReserved==result.reserved())
            throw new IllegalArgumentException("El ajuste no cambia el saldo actual.");
        movements.save(new StockMovement(product,form.getType(),form.getQuantity(),previous,previousReserved,form.getReason().trim(),actor));
        products.flush();
    }

    @Transactional(readOnly=true)
    public Page<StockMovement> history(Long id,int page) {
        return movements.findByProduct_IdOrderByCreatedAtDescIdDesc(id,PageRequest.of(Math.max(0,page),20));
    }
}
