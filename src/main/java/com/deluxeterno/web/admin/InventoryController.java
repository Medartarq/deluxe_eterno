package com.deluxeterno.web.admin;

import com.deluxeterno.domain.*;
import com.deluxeterno.dto.StockAdjustmentForm;
import com.deluxeterno.service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.util.Locale;

@Controller
@RequestMapping("/admin/inventory")
public class InventoryController {
    private final CatalogService catalog; private final InventoryService inventory;
    public InventoryController(CatalogService catalog,InventoryService inventory) { this.catalog=catalog; this.inventory=inventory; }

    @GetMapping
    public String list(@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="false") boolean low,Model model) {
        String search=q.trim().toLowerCase(Locale.ROOT);
        model.addAttribute("products",catalog.findAllProducts().stream()
                .filter(p -> search.isBlank() || (p.getName()+" "+p.getSku()).toLowerCase(Locale.ROOT).contains(search))
                .filter(p -> !low || p.isLowStock()).toList());
        model.addAttribute("q",q); model.addAttribute("low",low); return "admin/inventory";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,@RequestParam(defaultValue="0") int page,Model model) {
        var product=catalog.findAnyProduct(id); var form=new StockAdjustmentForm(); form.setVersion(product.getVersion());
        model.addAttribute("adjustment",form); populate(model,id,page); return "admin/stock-detail";
    }

    @PostMapping("/{id}")
    public String adjust(@PathVariable Long id,@Valid @ModelAttribute("adjustment") StockAdjustmentForm form,BindingResult errors,
            Model model,RedirectAttributes redirect,Principal principal) {
        if (!errors.hasErrors()) {
            try { inventory.adjust(id,form,principal.getName()); }
            catch (IllegalArgumentException exception) { errors.reject("stock.invalid",exception.getMessage()); }
        }
        if (errors.hasErrors()) { populate(model,id,0); return "admin/stock-detail"; }
        redirect.addFlashAttribute("successMessage","Movimiento registrado. El saldo y el historial se actualizaron juntos.");
        return "redirect:/admin/inventory/"+id;
    }
    private void populate(Model model,Long id,int page) {
        var product = catalog.findAnyProduct(id);
        model.addAttribute("product",product); model.addAttribute("movements",inventory.history(id,page));
        model.addAttribute("adminPublicUrl",product.isActive() ? "/productos/" + product.getId() : "/productos");
        model.addAttribute("movementTypes",StockMovementType.values());
    }
}
