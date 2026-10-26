package com.deluxeterno.config;

import com.deluxeterno.domain.Category;
import com.deluxeterno.domain.Product;
import com.deluxeterno.repository.CategoryRepository;
import com.deluxeterno.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Component
public class CatalogDataInitializer implements CommandLineRunner {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CatalogDataInitializer(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws IOException {
        Properties catalog = new Properties();
        try (var reader = new InputStreamReader(new ClassPathResource("catalog/products.properties").getInputStream(), StandardCharsets.UTF_8)) {
            catalog.load(reader);
        }
        for (String key : catalog.getProperty("products").split(",")) {
            // El identificador estable permite editar o desactivar desde el panel sin
            // que un reinicio repita registros ni sobrescriba decisiones del administrador.
            if (productRepository.existsByCatalogKey(key)) {
                continue;
            }
            String categoryKey = catalog.getProperty(key + ".category");
            String categoryName = catalog.getProperty("category." + categoryKey + ".name");
            Category category = categoryRepository.findByName(categoryName).orElseGet(() ->
                    categoryRepository.save(new Category(categoryName,
                            catalog.getProperty("category." + categoryKey + ".description"))));
            Product product = new Product(catalog.getProperty(key + ".name"),
                    catalog.getProperty(key + ".description"), catalog.getProperty(key + ".image"), category);
            product.setCatalogKey(key);
            product.setSecondaryImagePath(catalog.getProperty(key + ".secondaryImage"));
            productRepository.save(product);
        }
    }
}
