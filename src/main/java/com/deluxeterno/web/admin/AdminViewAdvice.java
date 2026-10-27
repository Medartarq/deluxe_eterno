package com.deluxeterno.web.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.deluxeterno.web.admin")
public class AdminViewAdvice {
    @ModelAttribute("adminUsername")
    public String username(java.security.Principal principal) { return principal == null ? "Administrador" : principal.getName(); }

    @ModelAttribute("adminPublicUrl")
    public String publicUrl(HttpServletRequest request) {
        String section = section(request);
        if (section.equals("products") || section.equals("inventory")) {
            String category = request.getParameter("categoryId");
            return "/productos" + (category != null && category.matches("[1-9][0-9]{0,17}") ? "?categoria=" + category : "");
        }
        if (section.equals("categories")) return "/#categorias";
        if (section.equals("inquiries")) return "/contacto";
        return "/";
    }

    @ModelAttribute("adminSection")
    public String section(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        for (String section : new String[]{"products", "inventory", "categories", "inquiries", "users", "demo"}) {
            if (path.equals("/admin/" + section) || path.startsWith("/admin/" + section + "/")) return section;
        }
        return "dashboard";
    }
}
