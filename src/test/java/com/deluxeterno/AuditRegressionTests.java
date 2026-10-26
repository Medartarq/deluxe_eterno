package com.deluxeterno;

import com.deluxeterno.domain.AdminUser;
import com.deluxeterno.domain.Category;
import com.deluxeterno.domain.Product;
import com.deluxeterno.domain.Role;
import com.deluxeterno.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import com.deluxeterno.service.CatalogService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuditRegressionTests {
    @Autowired MockMvc mvc;
    @Autowired CategoryRepository categories;
    @Autowired ProductRepository products;
    @Autowired InquiryRepository inquiries;
    @Autowired AdminUserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired EntityManager entityManager;
    @Autowired EntityManagerFactory entityManagerFactory;
    @Autowired CatalogService catalog;

    @Test void duplicateCategoryReturnsFieldErrorInsteadOfServerError() throws Exception {
        categories.saveAndFlush(new Category("Auditoria", "Original"));
        mvc.perform(post("/admin/categories").with(user("admin").roles("ADMIN")).with(csrf())
                .param("name", "  auditoria  ").param("description", "Duplicada"))
                .andExpect(status().isOk()).andExpect(view().name("admin/category-form"))
                .andExpect(model().attributeHasFieldErrors("categoryForm", "name"));
        assertThat(categories.findAll().stream().filter(c -> c.getName().trim().equalsIgnoreCase("Auditoria"))).hasSize(1);
    }

    @Test void renamingCategoryToExistingNameKeepsEditingContext() throws Exception {
        var first = categories.saveAndFlush(new Category("Original Auditoria", "A"));
        categories.saveAndFlush(new Category("Ocupada Auditoria", "B"));
        mvc.perform(post("/admin/categories/" + first.getId() + "/edit")
                .with(user("admin").roles("ADMIN")).with(csrf()).param("name", "Ocupada Auditoria"))
                .andExpect(status().isOk()).andExpect(model().attribute("editing", true))
                .andExpect(model().attribute("categoryId", first.getId()))
                .andExpect(model().attributeHasFieldErrors("categoryForm", "name"));
        assertThat(categories.findById(first.getId()).orElseThrow().getName()).isEqualTo("Original Auditoria");
    }

    @Test void missingInquiryReturnsNotFound() throws Exception {
        mvc.perform(post("/admin/inquiries/9223372036854775807/status")
                .with(user("admin").roles("ADMIN")).with(csrf()).param("status", "READ"))
                .andExpect(status().isNotFound());
    }

    @Test void inactiveProductCanBePublishedAgain() throws Exception {
        var category = categories.saveAndFlush(new Category("Reactivacion Auditoria", "A"));
        var product = products.saveAndFlush(new Product("Regalo auditoria", "Detalle", "/assets/gallery-1.svg", category));
        mvc.perform(post("/admin/products/" + product.getId() + "/delete")
                .with(user("admin").roles("ADMIN")).with(csrf())).andExpect(status().is3xxRedirection());
        mvc.perform(get("/productos/" + product.getId())).andExpect(status().isNotFound());
        mvc.perform(post("/admin/products/" + product.getId() + "/activate")
                .with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/products"));
        mvc.perform(get("/productos/" + product.getId())).andExpect(status().isOk())
                .andExpect(content().string(containsString("Regalo auditoria")));
    }

    @Test void staffAndMissingCsrfCannotModifyCatalog() throws Exception {
        long before = categories.count();
        mvc.perform(post("/admin/categories").with(user("staff").roles("STAFF")).with(csrf())
                .param("name", "No permitido")).andExpect(status().isForbidden());
        mvc.perform(post("/admin/categories").with(user("admin").roles("ADMIN"))
                .param("name", "Sin token")).andExpect(status().isForbidden());
        assertThat(categories.count()).isEqualTo(before);
    }

    @Test void invalidInquiryDoesNotPersistAndStoredMarkupIsEscaped() throws Exception {
        long before = inquiries.count();
        mvc.perform(post("/contactos").with(csrf()).param("name", "Cliente")
                .param("email", "incorrecto").param("message", "Consulta"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("inquiryForm", "email"));
        assertThat(inquiries.count()).isEqualTo(before);
        mvc.perform(post("/contactos").with(csrf()).param("name", "Cliente")
                .param("email", "prueba@example.com").param("message", "<script>alert(1)</script>"))
                .andExpect(status().is3xxRedirection());
        assertThat(inquiries.count()).isEqualTo(before + 1);
        mvc.perform(get("/admin/inquiries").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
    }

    @Test void loginUsesPersistedBcryptUser() throws Exception {
        users.saveAndFlush(new AdminUser("auditoria-login", encoder.encode("OnlyForThisTest123!"), Role.ADMIN));
        mvc.perform(formLogin().user("auditoria-login").password("OnlyForThisTest123!"))
                .andExpect(authenticated().withUsername("auditoria-login"))
                .andExpect(redirectedUrl("/admin"));
    }

    @Test void categoryWithProductsCannotBeDeleted() throws Exception {
        var category = categories.findByName("Flores").orElseThrow();
        mvc.perform(post("/admin/categories/" + category.getId() + "/delete")
                .with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(redirectedUrl("/admin/categories")).andExpect(flash().attributeExists("errorMessage"));
        assertThat(categories.existsById(category.getId())).isTrue();
    }

    @Test void staleCategoryIsReportedOnTheProductForm() throws Exception {
        mvc.perform(post("/admin/products").with(user("admin").roles("ADMIN")).with(csrf())
                .param("name", "Prueba").param("description", "Detalle").param("imagePath", "/assets/gallery-1.svg")
                .param("categoryId", "9223372036854775807"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("productForm", "categoryId"));
    }

    @Test void unsafeImageSchemeIsRejected() throws Exception {
        var category = categories.findByName("Flores").orElseThrow();
        mvc.perform(post("/admin/products").with(user("admin").roles("ADMIN")).with(csrf())
                .param("name", "Prueba").param("description", "Detalle").param("imagePath", "javascript:alert(1)")
                .param("categoryId", category.getId().toString()))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("productForm", "imagePath"));
    }

    @Test void productCreateAndEditPersistAndRenderTheCorrectFormAction() throws Exception {
        var category = categories.findByName("Flores").orElseThrow();
        mvc.perform(post("/admin/products").with(user("admin").roles("ADMIN")).with(csrf())
                .param("name", "Alta auditoria").param("description", "Detalle inicial")
                .param("imagePath", "https://example.com/imagen.jpg").param("categoryId", category.getId().toString()))
                .andExpect(redirectedUrl("/admin/products"));
        var created = products.findAll().stream().filter(p -> p.getName().equals("Alta auditoria")).findFirst().orElseThrow();
        mvc.perform(get("/admin/products/" + created.getId() + "/edit").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/admin/products/" + created.getId() + "/edit\"")));
        mvc.perform(post("/admin/products/" + created.getId() + "/edit")
                .with(user("admin").roles("ADMIN")).with(csrf()).param("name", "Edicion auditoria")
                .param("description", "Detalle actualizado").param("imagePath", "/assets/gallery-1.svg")
                .param("categoryId", category.getId().toString()))
                .andExpect(redirectedUrl("/admin/products"));
        products.flush(); entityManager.clear();
        assertThat(products.findById(created.getId()).orElseThrow().getDescription()).isEqualTo("Detalle actualizado");
    }

    @Test void categoryCanKeepItsNameAndMissingDeleteReturns404() throws Exception {
        var category = categories.saveAndFlush(new Category("Nombre auditoria", "Original"));
        mvc.perform(post("/admin/categories/" + category.getId() + "/edit")
                .with(user("admin").roles("ADMIN")).with(csrf())
                .param("name", " Nombre auditoria ").param("description", "Actualizada"))
                .andExpect(redirectedUrl("/admin/categories"));
        assertThat(categories.findById(category.getId()).orElseThrow().getDescription()).isEqualTo("Actualizada");
        mvc.perform(post("/admin/categories/9223372036854775807/delete")
                .with(user("admin").roles("ADMIN")).with(csrf())).andExpect(status().isNotFound());
    }

    @Test void homeLoadsOnlyThreeProductEntitiesAndDashboardLoadsNoCatalogEntities() throws Exception {
        var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        try {
            entityManager.clear(); statistics.clear();
            // Caracterización del algoritmo previo: la limitación se hacía en memoria.
            catalog.findActiveProducts(null).stream().limit(3).toList();
            long previousLoads = statistics.getEntityStatistics(Product.class.getName()).getLoadCount();
            entityManager.clear(); statistics.clear();
            mvc.perform(get("/")).andExpect(status().isOk());
            long currentLoads = statistics.getEntityStatistics(Product.class.getName()).getLoadCount();
            assertThat(previousLoads).isEqualTo(35);
            assertThat(currentLoads).isEqualTo(3);
            entityManager.clear(); statistics.clear();
            mvc.perform(get("/admin").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
            assertThat(statistics.getEntityStatistics(Product.class.getName()).getLoadCount()).isZero();
            assertThat(statistics.getEntityStatistics(Category.class.getName()).getLoadCount()).isZero();
        } finally { statistics.setStatisticsEnabled(false); }
    }

    @Test void customErrorPageAndPublicFaviconAreReachable() throws Exception {
        mvc.perform(get("/error").accept("text/html").requestAttr("jakarta.servlet.error.status_code", 404))
                .andExpect(status().isNotFound()).andExpect(view().name("error/404"))
                .andExpect(content().string(containsString("Volver al catálogo")));
        mvc.perform(get("/favicon.ico")).andExpect(status().isOk());
        mvc.perform(get("/assets/brand/favicon.svg")).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/nosotros", "/productos", "/servicios", "/galeria", "/contacto", "/login"})
    void everyPublicPageRendersWithoutAuthentication(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Deluxe Eterno")));
    }

    @Test void existingInquiryChangesStatusAndInvalidStatusIs400() throws Exception {
        var inquiry = inquiries.saveAndFlush(new com.deluxeterno.domain.Inquiry("Prueba", "prueba@example.com", "", "Consulta"));
        mvc.perform(post("/admin/inquiries/" + inquiry.getId() + "/status")
                .with(user("admin").roles("ADMIN")).with(csrf()).param("status", "RESOLVED"))
                .andExpect(redirectedUrl("/admin/inquiries"));
        assertThat(inquiries.findById(inquiry.getId()).orElseThrow().getStatus()).isEqualTo(com.deluxeterno.domain.InquiryStatus.RESOLVED);
        mvc.perform(post("/admin/inquiries/" + inquiry.getId() + "/status")
                .with(user("admin").roles("ADMIN")).with(csrf()).param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }
}
