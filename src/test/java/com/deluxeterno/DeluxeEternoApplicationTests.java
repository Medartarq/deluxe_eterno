package com.deluxeterno;

import com.deluxeterno.domain.Category;
import com.deluxeterno.domain.Product;
import com.deluxeterno.repository.CategoryRepository;
import com.deluxeterno.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DeluxeEternoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void publicHomeIsRendered() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/index"))
                .andExpect(content().string(containsString("Detalles personalizados")))
                .andExpect(content().string(not(containsString("Proyecto académico"))))
                .andExpect(content().string(not(containsString("Producto demostrativo"))));
    }

    @Test
    void catalogIsRendered() throws Exception {
        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/products"));
    }

    @Test
    void adminRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void inquiryIsValidatedAndRedirects() throws Exception {
        mockMvc.perform(post("/contactos")
                        .with(csrf())
                        .param("name", "Cliente de prueba")
                        .param("email", "cliente@example.com")
                        .param("phone", "999999999")
                        .param("message", "Consulta de prueba"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacto"));
    }

    @Test
    void adminPagesRenderForAdmin() throws Exception {
        mockMvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
        mockMvc.perform(get("/admin/products").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/products"));
        mockMvc.perform(get("/admin/inquiries").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/inquiries"));
    }

    @Test
    void adminFormsRenderForAdmin() throws Exception {
        mockMvc.perform(get("/admin/products/new").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"));
        mockMvc.perform(get("/admin/categories/new").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/category-form"));
    }

    @Test
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void customScriptIsIncludedInPublicPages() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/js/main.js")));
        mockMvc.perform(get("/nosotros"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/js/main.js")));
    }

    @Test
    void galleryIsReachableFromTheNavbar() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"mainNav\"")))
                .andExpect(content().string(containsString("href=\"/galeria\"")));
        mockMvc.perform(get("/galeria"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/gallery"));
    }

    @Test
    void catalogRendersBootstrapModalForEachProduct() throws Exception {
        Category category = categoryRepository.save(new Category("Modal", "Categoria de prueba"));
        Product product = productRepository.saveAndFlush(
                new Product("Producto con modal", "Descripcion", "/assets/gallery-1.svg", category));

        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-bs-toggle=\"modal\"")))
                .andExpect(content().string(containsString("productModal-" + product.getId())))
                .andExpect(content().string(containsString("modal-dialog-centered")));
    }

    @Test
    void inactiveProductCanStillBeEditedFromTheAdminPanel() throws Exception {
        Category category = categoryRepository.save(new Category("Edicion", "Categoria de prueba"));
        Product product = productRepository.saveAndFlush(
                new Product("Producto inactivo", "Descripcion", "/assets/gallery-2.svg", category));
        mockMvc.perform(post("/admin/products/" + product.getId() + "/delete")
                .with(user("admin").roles("ADMIN"))
                .with(csrf()));

        mockMvc.perform(get("/admin/products/" + product.getId() + "/edit").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"));
        mockMvc.perform(get("/productos/" + product.getId()))
                .andExpect(status().isNotFound());
    }
}
