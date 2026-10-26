package com.deluxeterno.web;

import com.deluxeterno.domain.Category;
import com.deluxeterno.domain.Product;
import com.deluxeterno.dto.InquiryForm;
import com.deluxeterno.service.CatalogService;
import com.deluxeterno.service.InquiryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class PublicController {

    private final CatalogService catalogService;
    private final InquiryService inquiryService;

    public PublicController(CatalogService catalogService, InquiryService inquiryService) {
        this.catalogService = catalogService;
        this.inquiryService = inquiryService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", catalogService.findCategories());
        model.addAttribute("products", catalogService.findFeaturedProducts());
        return "public/index";
    }

    @GetMapping("/nosotros")
    public String about() {
        return "public/about";
    }

    @GetMapping("/productos")
    public String products(@RequestParam(value = "categoria", required = false) Long categoryId, Model model) {
        List<Category> categories = catalogService.findCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("products", catalogService.findActiveProducts(categoryId));
        model.addAttribute("selectedCategory", categoryId);
        return "public/products";
    }

    @GetMapping("/productos/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = catalogService.findProduct(id);
        model.addAttribute("product", product);
        return "public/product-detail";
    }

    @GetMapping("/servicios")
    public String services() {
        return "public/services";
    }

    @GetMapping("/galeria")
    public String gallery(@RequestParam(value = "categoria", required = false) Long categoryId, Model model) {
        model.addAttribute("products", catalogService.findActiveProducts(categoryId));
        model.addAttribute("categories", catalogService.findCategories());
        model.addAttribute("selectedCategory", categoryId);
        return "public/gallery";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/contacto")
    public String contact(Model model) {
        if (!model.containsAttribute("inquiryForm")) {
            model.addAttribute("inquiryForm", new InquiryForm());
        }
        return "public/contact";
    }

    @PostMapping("/contactos")
    public String submitInquiry(
            @Valid @ModelAttribute("inquiryForm") InquiryForm inquiryForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "public/contact";
        }
        inquiryService.create(inquiryForm);
        redirectAttributes.addFlashAttribute("successMessage", "Tu consulta fue registrada correctamente.");
        return "redirect:/contacto";
    }
}
