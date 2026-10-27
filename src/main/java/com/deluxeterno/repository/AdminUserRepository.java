package com.deluxeterno.repository;

import com.deluxeterno.domain.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    List<AdminUser> findAllByOrderByUsernameAsc();
}
