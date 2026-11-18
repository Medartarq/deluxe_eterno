package com.deluxeterno.config;

import com.deluxeterno.domain.AdminUser;
import com.deluxeterno.domain.Role;
import com.deluxeterno.repository.AdminUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminUserInitializerTests {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void resetsExistingAdministratorOnlyWhenExplicitlyRequested() throws Exception {
        AdminUserRepository users = mock(AdminUserRepository.class);
        AdminUser existing = new AdminUser("admin", encoder.encode("old-password"), Role.ADMIN);
        when(users.findByUsername("admin")).thenReturn(Optional.of(existing));

        new AdminUserInitializer(users, encoder, "admin", "new-secure-password", true).run();

        assertThat(encoder.matches("new-secure-password", existing.getPassword())).isTrue();
        verify(users).save(existing);
    }

    @Test
    void createsAdministratorWhenResetIsRequestedForMissingUsername() throws Exception {
        AdminUserRepository users = mock(AdminUserRepository.class);
        when(users.findByUsername("admin")).thenReturn(Optional.empty());

        new AdminUserInitializer(users, encoder, "admin", "new-secure-password", true).run();

        verify(users).save(any(AdminUser.class));
    }
}
