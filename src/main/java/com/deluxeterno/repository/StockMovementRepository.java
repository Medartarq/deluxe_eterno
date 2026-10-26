package com.deluxeterno.repository;

import com.deluxeterno.domain.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockMovementRepository extends JpaRepository<StockMovement,Long> {
    Page<StockMovement> findByProduct_IdOrderByCreatedAtDescIdDesc(Long productId, Pageable pageable);
}
