package com.deluxeterno;

import com.deluxeterno.domain.Category;
import com.deluxeterno.domain.Inquiry;
import com.deluxeterno.domain.InquiryStatus;
import com.deluxeterno.domain.Product;
import com.deluxeterno.domain.SupplyMode;
import com.deluxeterno.repository.CategoryRepository;
import com.deluxeterno.repository.InquiryRepository;
import com.deluxeterno.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminPaginationTests {
    @Autowired MockMvc mvc;
    @Autowired CategoryRepository categories;
    @Autowired ProductRepository products;
    @Autowired InquiryRepository inquiries;

    @Test
    void productAndInventoryFiltersArePaginatedInTheDatabase() throws Exception {
        Category category = categories.saveAndFlush(new Category("Paginación", "Prueba"));
        for (int index = 0; index < 16; index++) {
            Product product = new Product("Producto paginado " + index, "Descripción", "/assets/gallery-1.svg", category);
            product.setSupplyMode(SupplyMode.STOCK);
            product.setStockQuantity(index + 1);
            products.save(product);
        }
        products.flush();

        Page<?> productPage = pageFrom("/admin/products", "products", "Producto paginado", 1, null);
        assertThat(productPage.getTotalElements()).isEqualTo(16);
        assertThat(productPage.getContent()).hasSize(1);

        Page<?> inventoryPage = pageFrom("/admin/inventory", "products", "Producto paginado", 1, null);
        assertThat(inventoryPage.getTotalElements()).isEqualTo(16);
        assertThat(inventoryPage.getContent()).hasSize(1);
    }

    @Test
    void inquiriesCanBeFilteredAndPaginated() throws Exception {
        for (int index = 0; index < 21; index++) {
            Inquiry inquiry = new Inquiry("Cliente paginado " + index, "cliente" + index + "@example.com", "", "Mensaje paginado");
            if (index == 0) inquiry.setStatus(InquiryStatus.RESOLVED);
            inquiries.save(inquiry);
        }
        inquiries.flush();

        Page<?> page = pageFrom("/admin/inquiries", "inquiries", "Cliente paginado", 1, null);
        assertThat(page.getTotalElements()).isEqualTo(21);
        assertThat(page.getContent()).hasSize(1);

        Page<?> resolved = pageFrom("/admin/inquiries", "inquiries", "Cliente paginado", 0, "RESOLVED");
        assertThat(resolved.getTotalElements()).isEqualTo(1);
    }

    private Page<?> pageFrom(String path, String modelAttribute, String search, int page, String status) throws Exception {
        var request = get(path)
                .param("q", search)
                .param("page", String.valueOf(page));
        if (status != null) {
            request.param("status", status);
        }
        var result = mvc.perform(request.with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andReturn();
        return (Page<?>) result.getModelAndView().getModel().get(modelAttribute);
    }
}
