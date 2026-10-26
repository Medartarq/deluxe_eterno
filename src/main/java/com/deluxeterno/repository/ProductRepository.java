package com.deluxeterno.repository;

import com.deluxeterno.domain.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

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

    long countByPublicationStatus(com.deluxeterno.domain.PublicationStatus status);

    @Query("select count(p) from Product p where p.supplyMode = :mode and (p.stockQuantity - p.reservedQuantity) <= p.lowStockThreshold")
    long countLowStock(@Param("mode") com.deluxeterno.domain.SupplyMode mode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findForUpdate(@Param("id") Long id);
}
