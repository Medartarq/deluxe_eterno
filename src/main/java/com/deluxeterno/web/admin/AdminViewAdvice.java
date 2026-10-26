package com.deluxeterno.web.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.deluxeterno.web.admin")
public class AdminViewAdvice {
    @ModelAttribute("adminUsername")
    public String username(java.security.Principal principal) { return principal == null ? "Administrador" : principal.getName(); }

    @ModelAttribute("adminSection")
    public String section(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        for (String section : new String[]{"products", "inventory", "categories", "inquiries", "demo"}) {
            if (path.equals("/admin/" + section) || path.startsWith("/admin/" + section + "/")) return section;
        }
        return "dashboard";
    }
}
