package com.deluxeterno.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Accesos internos derivados de rutas conocidas; nunca del Referer o de una URL enviada por el cliente. */
@ControllerAdvice
public class WorkspaceNavigationAdvice {
    public record Access(boolean administrator, String managementUrl, String managementLabel) {}

    @ModelAttribute("workspaceAccess")
    public Access access(HttpServletRequest request) {
        if (!request.isUserInRole("ADMIN")) return new Access(false, null, null);
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.matches("/productos/[0-9]+")) {
            String id = path.substring("/productos/".length());
            return new Access(true, "/admin/products/" + id + "/edit", "Editar este producto");
        }
        if (path.equals("/") || path.equals("/productos") || path.equals("/galeria")) {
            String category = request.getParameter("categoria");
            String filter = category != null && category.matches("[1-9][0-9]{0,17}") ? "?categoryId=" + category : "";
            return new Access(true, "/admin/products" + filter, "Gestionar catálogo");
        }
        if (path.equals("/contacto")) return new Access(true, "/admin/inquiries", "Ver consultas");
        return new Access(true, null, null);
    }
}
