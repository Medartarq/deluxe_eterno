package com.deluxeterno.repository;

import com.deluxeterno.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOrderByNameAsc();

    Optional<Category> findByName(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("select distinct p.category from Product p where p.active = true order by p.category.name")
    List<Category> findPublishedCategories();
}
