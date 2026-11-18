package com.deluxeterno.config;

import com.deluxeterno.domain.AdminUser;
import com.deluxeterno.domain.Role;
import com.deluxeterno.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Component
@org.springframework.context.annotation.Profile("!demo")
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;
    private final boolean passwordResetRequested;

    public AdminUserInitializer(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${ADMIN_USERNAME:${app.admin.username:admin}}") String username,
            @Value("${ADMIN_PASSWORD:${app.admin.password:}}") String password,
            @Value("${ADMIN_PASSWORD_RESET:false}") boolean passwordResetRequested) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
        this.passwordResetRequested = passwordResetRequested;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (password.isBlank()) {
            return;
        }

        if (passwordResetRequested) {
            Optional<AdminUser> existingUser = adminUserRepository.findByUsername(username);
            AdminUser user;
            if (existingUser.isPresent()) {
                user = existingUser.get();
                user.resetPassword(passwordEncoder.encode(password));
            } else {
                user = new AdminUser(username, passwordEncoder.encode(password), Role.ADMIN);
            }
            adminUserRepository.save(user);
            logger.warn("La contraseña del administrador '{}' fue restablecida mediante ADMIN_PASSWORD_RESET.", username);
            return;
        }

        if (adminUserRepository.count() == 0) {
            adminUserRepository.save(new AdminUser(username, passwordEncoder.encode(password), Role.ADMIN));
        }
    }
}
