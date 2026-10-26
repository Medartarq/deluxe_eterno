package com.deluxeterno;

import com.deluxeterno.config.CatalogDataInitializer;
import com.deluxeterno.repository.ProductRepository;
import com.deluxeterno.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductCatalogIntegrationTests {
    @Autowired ProductRepository products;
    @Autowired CategoryRepository categories;
    @Autowired CatalogDataInitializer initializer;
    @Autowired MockMvc mvc;

    @Test
    void imports35ProductsWithExistingPhotosAndPreservesAdminChangesOnRestart() throws Exception {
        var imported = products.findAll().stream().filter(p -> p.getCatalogKey() != null).toList();
        assertThat(imported).hasSize(35);
        for (var product : imported) {
            assertThat(new ClassPathResource("static" + product.getImagePath()).exists()).isTrue();
        }
        var edited = products.findByCatalogKey("DE-001").orElseThrow();
        edited.setName("Nombre editado desde el panel");
        edited.setActive(false);
        products.saveAndFlush(edited);
        long before = products.count();
        initializer.run();
        assertThat(products.count()).isEqualTo(before);
        var preserved = products.findByCatalogKey("DE-001").orElseThrow();
        assertThat(preserved.getName()).isEqualTo("Nombre editado desde el panel");
        assertThat(preserved.isActive()).isFalse();
        mvc.perform(get("/productos/" + preserved.getId())).andExpect(status().isNotFound());
    }

    @Test
    void categoryFilterOnlyShowsMatchingProducts() throws Exception {
        var flowers = categories.findByName("Flores").orElseThrow();
        mvc.perform(get("/productos").param("categoria", flowers.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cielo de Girasoles")))
                .andExpect(content().string(not(containsString("Abrazo de Koala"))));
    }

    @Test
    void galleryShowsProductsAndCollectionAndDetailKeepsSecondView() throws Exception {
        mvc.perform(get("/galeria"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cielo de Girasoles")))
                .andExpect(content().string(containsString("Pequeños Grandes Detalles")))
                .andExpect(content().string(not(containsString("Galería en actualización"))));
        var route = products.findByCatalogKey("DE-016").orElseThrow();
        assertThat(route.getSecondaryImagePath()).endsWith("ruta-de-aventuras-vista-02.jpg");
        assertThat(new ClassPathResource("static" + route.getSecondaryImagePath()).exists()).isTrue();
        mvc.perform(get("/productos/" + route.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ruta-de-aventuras-vista-02.jpg")));
    }
}
