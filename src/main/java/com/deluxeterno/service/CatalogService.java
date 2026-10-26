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
import java.util.UUID;
import com.deluxeterno.domain.*;

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

    public long countDrafts() { return productRepository.countByPublicationStatus(PublicationStatus.DRAFT); }

    public long countLowStock() { return productRepository.countLowStock(SupplyMode.STOCK); }

    public long countProducts() { return productRepository.count(); }

    public long countCategories() { return categoryRepository.count(); }

    public boolean categoryExists(Long id) { return categoryRepository.existsById(id); }

    public Product findProduct(Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        product.getAdditionalImagePaths().size();
        return product;
    }

    public Product findAnyProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        product.getAdditionalImagePaths().size();
        return product;
    }

    @Transactional
    public Product createProduct(ProductForm form) {
        return createProduct(form, "sistema");
    }

    @Transactional
    public Product createProduct(ProductForm form, String actor) {
        Product product = new Product(
                form.getName(),
                form.getDescription(),
                form.getImagePath(),
                findCategory(form.getCategoryId()));
        product.setSku("DE-" + UUID.randomUUID().toString().replace("-", "").substring(0,12).toUpperCase());
        applyForm(product,form,actor);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductForm form) {
        return updateProduct(id,form,"sistema");
    }

    @Transactional
    public Product updateProduct(Long id, ProductForm form, String actor) {
        Product product = productRepository.findForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        if (form.getVersion()!=null && form.getVersion()!=product.getVersion())
            throw new ProductEditException("Otra persona modificó este producto. Recarga la página antes de guardar.");
        if (product.getSupplyMode()==SupplyMode.STOCK && form.getSupplyMode()==SupplyMode.MADE_TO_ORDER
                && (product.getStockQuantity()>0 || product.getReservedQuantity()>0))
            throw new ProductEditException("Antes de cambiar a por encargo, libera las reservas y ajusta la existencia física a cero.");
        product.setName(form.getName());
        product.setDescription(form.getDescription());
        product.setImagePath(form.getImagePath());
        product.setCategory(findCategory(form.getCategoryId()));
        applyForm(product,form,actor);
        return productRepository.save(product);
    }

    private void applyForm(Product product, ProductForm form, String actor) {
        product.setName(form.getName().trim());
        product.setDescription(form.getDescription()==null ? "" : form.getDescription().trim());
        product.setImagePath(form.getImagePath()==null ? "" : form.getImagePath().trim());
        product.setSupplyMode(form.getSupplyMode()); product.setPublicationStatus(form.getPublicationStatus());
        product.setPriceMode(form.getPriceMode()); product.setPrice(form.getPriceMode()==PriceMode.CONSULT ? null : form.getPrice());
        product.setIncludedItems(form.getIncludedItems()); product.setPersonalization(form.getPersonalization());
        product.setOccasions(form.getOccasions()); product.setPreparationTime(form.getPreparationTime());
        product.setInternalNotes(form.getInternalNotes()); product.setTemporarilyUnavailable(form.isTemporarilyUnavailable());
        product.setLowStockThreshold(form.getLowStockThreshold()); product.setUpdatedBy(actor);
        product.setFocusX(form.getFocusX()); product.setFocusY(form.getFocusY());
        if (form.isGallerySubmitted()) {
            product.setSecondaryImagePath(null);
            product.getAdditionalImagePaths().clear();
            form.getAdditionalImagePaths().stream().filter(s -> !s.equals(form.getImagePath())).distinct().forEach(product.getAdditionalImagePaths()::add);
        }
    }

    @Transactional
    public void deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        product.setActive(false);
    }

    @Transactional
    public void activateProduct(Long id) {
        Product product=findAnyProduct(id);
        if (product.getDescription().isBlank() || product.getImagePath().isBlank())
            throw new ProductEditException("Completa la descripción y la fotografía principal antes de publicar.");
        product.setActive(true);
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
