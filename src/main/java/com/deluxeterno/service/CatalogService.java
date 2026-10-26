package com.deluxeterno.service;

import com.deluxeterno.domain.Category;
import com.deluxeterno.domain.Product;
import com.deluxeterno.dto.CategoryForm;
import com.deluxeterno.dto.ProductForm;
import com.deluxeterno.repository.CategoryRepository;
import com.deluxeterno.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CatalogService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<Category> findCategories() {
        return categoryRepository.findPublishedCategories();
    }

    public List<Category> findAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    public List<Product> findActiveProducts(Long categoryId) {
        return categoryId == null
                ? productRepository.findAllByActiveTrueOrderByNameAsc()
                : productRepository.findByActiveTrueAndCategory_IdOrderByNameAsc(categoryId);
    }

    public List<Product> findAllProducts() {
        return productRepository.findAllByOrderByNameAsc();
    }

    public List<Product> findFeaturedProducts() {
        return productRepository.findTop3ByActiveTrueOrderByNameAsc();
    }

    public long countProducts() { return productRepository.count(); }

    public long countCategories() { return categoryRepository.count(); }

    public boolean categoryExists(Long id) { return categoryRepository.existsById(id); }

    public Product findProduct(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    public Product findAnyProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    @Transactional
    public Product createProduct(ProductForm form) {
        Product product = new Product(
                form.getName(),
                form.getDescription(),
                form.getImagePath(),
                findCategory(form.getCategoryId()));
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductForm form) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        product.setName(form.getName());
        product.setDescription(form.getDescription());
        product.setImagePath(form.getImagePath());
        product.setCategory(findCategory(form.getCategoryId()));
        return productRepository.save(product);
    }

    @Transactional
    public void deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        product.setActive(false);
    }

    @Transactional
    public void activateProduct(Long id) {
        findAnyProduct(id).setActive(true);
    }

    @Transactional
    public Category createCategory(CategoryForm form) {
        String name = form.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateCategoryException();
        }
        return saveCategory(new Category(name, form.getDescription()));
    }

    @Transactional
    public Category updateCategory(Long id, CategoryForm form) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
        String name = form.getName().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateCategoryException();
        }
        category.setName(name);
        category.setDescription(form.getDescription());
        return saveCategory(category);
    }

    private Category saveCategory(Category category) {
        try {
            // Resolver también una carrera entre dos altas antes de salir del servicio.
            return categoryRepository.saveAndFlush(category);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateCategoryException();
        }
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategory(id);
        if (productRepository.existsByCategory_Id(id)) {
            throw new IllegalStateException("No se puede eliminar una categoría con productos asociados.");
        }
        categoryRepository.delete(category);
    }

    public Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
    }
}
