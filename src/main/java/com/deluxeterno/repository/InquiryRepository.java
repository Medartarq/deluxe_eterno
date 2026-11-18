package com.deluxeterno.repository;

import com.deluxeterno.domain.Inquiry;
import com.deluxeterno.domain.InquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    List<Inquiry> findAllByOrderByCreatedAtDesc();

    @Query("""
            select i from Inquiry i
            where (:search = '' or lower(i.name) like lower(concat('%', :search, '%'))
                   or lower(i.email) like lower(concat('%', :search, '%'))
                   or lower(i.message) like lower(concat('%', :search, '%')))
              and (:status is null or i.status = :status)
            """)
    Page<Inquiry> findAdminInquiries(
            @Param("search") String search,
            @Param("status") InquiryStatus status,
            Pageable pageable);

    long countByStatus(InquiryStatus status);
}
