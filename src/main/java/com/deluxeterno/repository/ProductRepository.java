package com.deluxeterno.repository;

import com.deluxeterno.domain.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = "category")
    List<Product> findAllByActiveTrueOrderByNameAsc();

    @EntityGraph(attributePaths = "category")
    List<Product> findTop3ByActiveTrueOrderByNameAsc();

    @EntityGraph(attributePaths = "category")
    List<Product> findByActiveTrueAndCategory_IdOrderByNameAsc(Long categoryId);

    @EntityGraph(attributePaths = "category")
    List<Product> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = "category")
    Optional<Product> findByIdAndActiveTrue(Long id);

    boolean existsByCategory_Id(Long categoryId);

    boolean existsByCatalogKey(String catalogKey);

    Optional<Product> findByCatalogKey(String catalogKey);
}
