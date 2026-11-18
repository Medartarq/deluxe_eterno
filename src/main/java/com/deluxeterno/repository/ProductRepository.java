package com.deluxeterno.repository;

import com.deluxeterno.domain.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Query("""
            select p from Product p
            where (:search = '' or lower(p.name) like lower(concat('%', :search, '%'))
                   or lower(p.sku) like lower(concat('%', :search, '%')))
              and (:categoryId is null or p.category.id = :categoryId)
              and (:status is null or p.publicationStatus = :status)
              and (:mode is null or p.supplyMode = :mode)
            """)
    Page<Product> findAdminProducts(
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("status") com.deluxeterno.domain.PublicationStatus status,
            @Param("mode") com.deluxeterno.domain.SupplyMode mode,
            Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("""
            select p from Product p
            where (:search = '' or lower(p.name) like lower(concat('%', :search, '%'))
                   or lower(p.sku) like lower(concat('%', :search, '%')))
              and (:lowOnly = false or (p.supplyMode = :stockMode
                   and (p.stockQuantity - p.reservedQuantity) <= p.lowStockThreshold))
            """)
    Page<Product> findInventoryProducts(
            @Param("search") String search,
            @Param("lowOnly") boolean lowOnly,
            @Param("stockMode") com.deluxeterno.domain.SupplyMode stockMode,
            Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findByIdAndActiveTrue(Long id);

    boolean existsByCategory_Id(Long categoryId);

    boolean existsByCatalogKey(String catalogKey);

    Optional<Product> findByCatalogKey(String catalogKey);

    long countByPublicationStatus(com.deluxeterno.domain.PublicationStatus status);

    long countByActiveTrue();

    @Query("select count(p) from Product p where p.supplyMode = :mode and (p.stockQuantity - p.reservedQuantity) <= p.lowStockThreshold")
    long countLowStock(@Param("mode") com.deluxeterno.domain.SupplyMode mode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findForUpdate(@Param("id") Long id);
}
