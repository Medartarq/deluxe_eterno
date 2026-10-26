package com.deluxeterno.web.admin;

import com.deluxeterno.domain.*;
import com.deluxeterno.dto.ProductForm;
import com.deluxeterno.service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/admin/products")
public class ProductAdminController {
    private final CatalogService catalog;
    public ProductAdminController(CatalogService catalog) { this.catalog=catalog; }

    @GetMapping
    public String list(@RequestParam(defaultValue="") String q,
            @RequestParam(required=false) Long categoryId, @RequestParam(required=false) PublicationStatus status,
            @RequestParam(required=false) SupplyMode mode, Model model) {
        var all=catalog.findAllProducts();
        String search=q.trim().toLowerCase(Locale.ROOT);
        model.addAttribute("products",all.stream().filter(p -> search.isBlank() || (p.getName()+" "+p.getSku()).toLowerCase(Locale.ROOT).contains(search))
                .filter(p -> categoryId==null || p.getCategory().getId().equals(categoryId))
                .filter(p -> status==null || p.getPublicationStatus()==status).filter(p -> mode==null || p.getSupplyMode()==mode).toList());
        model.addAttribute("total",all.size());
        model.addAttribute("published",all.stream().filter(Product::isActive).count());
        model.addAttribute("drafts",all.stream().filter(p -> p.getPublicationStatus()==PublicationStatus.DRAFT).count());
        model.addAttribute("lowStock",all.stream().filter(Product::isLowStock).count());
        model.addAttribute("q",q); model.addAttribute("selectedCategory",categoryId); model.addAttribute("selectedStatus",status); model.addAttribute("selectedMode",mode);
        options(model);
        return "admin/products";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        var form=new ProductForm(); form.setPublicationStatus(PublicationStatus.DRAFT); form.setGallerySubmitted(true);
        model.addAttribute("productForm",form); prepare(model,null);
        return "admin/product-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("productForm",toForm(catalog.findAnyProduct(id))); prepare(model,id);
        return "admin/product-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("productForm") ProductForm form, BindingResult errors,
            Model model, RedirectAttributes redirect, Principal principal) {
        validateCategory(form,errors);
        if (errors.hasErrors()) { prepare(model,null); return "admin/product-form"; }
        try { catalog.createProduct(form,principal.getName()); }
        catch (ProductEditException exception) { errors.reject("product.invalid",exception.getMessage()); prepare(model,null); return "admin/product-form"; }
        redirect.addFlashAttribute("successMessage","Producto guardado correctamente.");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("productForm") ProductForm form, BindingResult errors,
            Model model, RedirectAttributes redirect, Principal principal) {
        validateCategory(form,errors);
        if (errors.hasErrors()) { prepare(model,id); return "admin/product-form"; }
        try { catalog.updateProduct(id,form,principal.getName()); }
        catch (ProductEditException exception) { errors.reject("product.invalid",exception.getMessage()); prepare(model,id); return "admin/product-form"; }
        redirect.addFlashAttribute("successMessage","Producto actualizado correctamente.");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String archive(@PathVariable Long id, RedirectAttributes redirect) {
        catalog.deactivateProduct(id); redirect.addFlashAttribute("successMessage","Producto archivado; se conserva su información e historial.");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/activate")
    public String publish(@PathVariable Long id, RedirectAttributes redirect) {
        try { catalog.activateProduct(id); redirect.addFlashAttribute("successMessage","Producto publicado correctamente."); }
        catch (ProductEditException exception) { redirect.addFlashAttribute("errorMessage",exception.getMessage()); }
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/duplicate")
    public String duplicate(@PathVariable Long id, RedirectAttributes redirect, Principal principal) {
        var form=toForm(catalog.findAnyProduct(id));
        form.setName(("Copia de "+form.getName()).substring(0,Math.min(120,9+form.getName().length())));
        form.setPublicationStatus(PublicationStatus.DRAFT); form.setVersion(null);
        var copy=catalog.createProduct(form,principal.getName());
        redirect.addFlashAttribute("successMessage","Copia creada como borrador, sin existencias ni reservas.");
        return "redirect:/admin/products/"+copy.getId()+"/edit";
    }

    private void validateCategory(ProductForm form, BindingResult errors) {
        if (!errors.hasFieldErrors("categoryId") && form.getCategoryId()!=null && !catalog.categoryExists(form.getCategoryId()))
            errors.rejectValue("categoryId","category.missing","La categoría ya no está disponible. Selecciona otra.");
    }
    private void options(Model model) {
        model.addAttribute("categories",catalog.findAllCategories()); model.addAttribute("statuses",PublicationStatus.values());
        model.addAttribute("supplyModes",SupplyMode.values()); model.addAttribute("priceModes",PriceMode.values());
    }
    private void prepare(Model model,Long id) {
        options(model); model.addAttribute("editing",id!=null); model.addAttribute("productId",id);
        model.addAttribute("sku",id==null?"Se genera al guardar":catalog.findAnyProduct(id).getSku());
        model.addAttribute("photoLibrary",catalog.findAllProducts().stream().filter(p -> p.getImagePath()!=null && !p.getImagePath().isBlank()).limit(100).toList());
    }
    private ProductForm toForm(Product p) {
        var f=new ProductForm(); f.setName(p.getName()); f.setDescription(p.getDescription()); f.setImagePath(p.getImagePath());
        f.setCategoryId(p.getCategory().getId()); f.setPublicationStatus(p.getPublicationStatus()); f.setSupplyMode(p.getSupplyMode());
        f.setPriceMode(p.getPriceMode()); f.setPrice(p.getPrice()); f.setIncludedItems(p.getIncludedItems()); f.setPersonalization(p.getPersonalization());
        f.setOccasions(p.getOccasions()); f.setPreparationTime(p.getPreparationTime()); f.setInternalNotes(p.getInternalNotes());
        f.setTemporarilyUnavailable(p.isTemporarilyUnavailable()); f.setLowStockThreshold(p.getLowStockThreshold()); f.setVersion(p.getVersion());
        f.setFocusX(p.getFocusX()); f.setFocusY(p.getFocusY()); f.setGallerySubmitted(true);
        var gallery=new ArrayList<>(p.getGalleryImages()); if (!gallery.isEmpty()) gallery.remove(0); f.setAdditionalImagePaths(gallery);
        return f;
    }
}
