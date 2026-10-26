package com.deluxeterno.web.admin;

import com.deluxeterno.domain.InquiryStatus;
import com.deluxeterno.dto.CategoryForm;
import com.deluxeterno.service.CatalogService;
import com.deluxeterno.service.InquiryService;
import com.deluxeterno.service.DuplicateCategoryException;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CatalogService catalogService;
    private final InquiryService inquiryService;

    public AdminController(CatalogService catalogService, InquiryService inquiryService) {
        this.catalogService = catalogService;
        this.inquiryService = inquiryService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("newInquiryCount", inquiryService.countNew());
        model.addAttribute("totalInquiryCount", inquiryService.countAll());
        model.addAttribute("draftCount", catalogService.countDrafts());
        model.addAttribute("lowStockCount", catalogService.countLowStock());
        model.addAttribute("productCount", catalogService.countProducts());
        model.addAttribute("categoryCount", catalogService.countCategories());
        return "admin/dashboard";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", catalogService.findAllCategories());
        return "admin/categories";
    }

    @GetMapping("/categories/new")
    public String newCategory(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        return "admin/category-form";
    }

    @PostMapping("/categories")
    public String createCategory(
            @Valid @ModelAttribute("categoryForm") CategoryForm categoryForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/category-form";
        }
        try {
            catalogService.createCategory(categoryForm);
        } catch (DuplicateCategoryException exception) {
            bindingResult.rejectValue("name", "category.duplicate", exception.getMessage());
            return "admin/category-form";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Categoría creada correctamente.");
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategory(@PathVariable Long id, Model model) {
        CategoryForm form = new CategoryForm();
        var category = catalogService.findCategory(id);
        form.setName(category.getName());
        form.setDescription(category.getDescription());
        model.addAttribute("categoryForm", form);
        model.addAttribute("categoryId", id);
        model.addAttribute("editing", true);
        return "admin/category-form";
    }

    @PostMapping("/categories/{id}/edit")
    public String updateCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute("categoryForm") CategoryForm categoryForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categoryId", id);
            model.addAttribute("editing", true);
            return "admin/category-form";
        }
        try {
            catalogService.updateCategory(id, categoryForm);
        } catch (DuplicateCategoryException exception) {
            bindingResult.rejectValue("name", "category.duplicate", exception.getMessage());
            model.addAttribute("categoryId", id);
            model.addAttribute("editing", true);
            return "admin/category-form";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Categoría actualizada correctamente.");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            catalogService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Categoría eliminada correctamente.");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "La categoría tiene registros asociados y no se puede eliminar.");
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/inquiries")
    public String inquiries(Model model) {
        model.addAttribute("inquiries", inquiryService.findAll());
        return "admin/inquiries";
    }

    @PostMapping("/inquiries/{id}/status")
    public String updateInquiryStatus(
            @PathVariable Long id,
            @RequestParam InquiryStatus status,
            RedirectAttributes redirectAttributes) {
        inquiryService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Estado de la consulta actualizado.");
        return "redirect:/admin/inquiries";
    }
}
