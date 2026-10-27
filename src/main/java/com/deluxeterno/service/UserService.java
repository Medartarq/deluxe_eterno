package com.deluxeterno.service;

import com.deluxeterno.domain.AdminUser;
import com.deluxeterno.domain.Role;
import com.deluxeterno.dto.UserForm;
import com.deluxeterno.repository.AdminUserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class UserService {
    // Las vistas reciben únicamente información pública de la cuenta, nunca el hash.
    public record Account(Long id, String username, Role role, boolean enabled) {}

    private final AdminUserRepository users;
    private final PasswordEncoder encoder;

    public UserService(AdminUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public List<Account> findAll() {
        return users.findAllByOrderByUsernameAsc().stream()
                .map(user -> new Account(user.getId(), user.getUsername(), user.getRole(), user.isEnabled()))
                .toList();
    }

    @Transactional
    public void create(UserForm form) {
        String username = form.getUsername().toLowerCase(Locale.ROOT);
        if (users.existsByUsernameIgnoreCase(username)) throw new DuplicateUsernameException();
        try {
            // ADMIN es el único rol con acceso al panel; no se acepta un rol del cliente.
            users.saveAndFlush(new AdminUser(username, encoder.encode(form.getPassword()), Role.ADMIN));
        } catch (DataIntegrityViolationException exception) {
            // También resuelve altas simultáneas con el mismo nombre.
            throw new DuplicateUsernameException();
        }
    }
}
