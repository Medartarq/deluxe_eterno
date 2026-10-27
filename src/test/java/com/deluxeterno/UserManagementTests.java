package com.deluxeterno;

import com.deluxeterno.domain.AdminUser;
import com.deluxeterno.domain.Role;
import com.deluxeterno.repository.AdminUserRepository;
import com.deluxeterno.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserManagementTests {
    private static final String PASSWORD = "ClaveDePrueba!2026";
    @Autowired MockMvc mvc;
    @Autowired AdminUserRepository users;
    @Autowired PasswordEncoder encoder;

    @Test void viewsRenderAndListDoesNotExposePasswordHashes() throws Exception {
        var account = users.saveAndFlush(new AdminUser("cuenta.visible", encoder.encode(PASSWORD), Role.ADMIN));
        var result = mvc.perform(get("/admin/users").with(user("owner").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(view().name("admin/users")).andReturn();
        assertThat(result.getResponse().getContentAsString()).contains("cuenta.visible").doesNotContain(account.getPassword(), PASSWORD);
        assertThat((java.util.List<?>)result.getModelAndView().getModel().get("users"))
                .allMatch(item -> item instanceof UserService.Account);
        mvc.perform(get("/admin/users/new").with(user("owner").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Confirmar contraseña")));
    }

    @Test void administratorCreatesAccountWithHashAndNewAccountCanLogin() throws Exception {
        mvc.perform(post("/admin/users").with(user("owner").roles("ADMIN")).with(csrf())
                .param("username", "Equipo.Alfa").param("password", PASSWORD).param("passwordConfirmation", PASSWORD))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/users"));
        var created = users.findByUsername("equipo.alfa").orElseThrow();
        assertThat(created.getPassword()).isNotEqualTo(PASSWORD);
        assertThat(encoder.matches(PASSWORD, created.getPassword())).isTrue();
        assertThat(created.getRole()).isEqualTo(Role.ADMIN);
        assertThat(created.isEnabled()).isTrue();
        mvc.perform(post("/login").with(csrf()).param("username", "equipo.alfa").param("password", PASSWORD))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin"))
                .andExpect(authenticated().withUsername("equipo.alfa").withRoles("ADMIN"));
    }

    @Test void duplicateNameIgnoringCasePreservesExistingAccountAndDoesNotEchoSecrets() throws Exception {
        var existing = users.saveAndFlush(new AdminUser("cuenta.existente", encoder.encode(PASSWORD), Role.ADMIN));
        long count = users.count();
        String originalHash = existing.getPassword();
        mvc.perform(post("/admin/users").with(user("owner").roles("ADMIN")).with(csrf())
                .param("username", "CUENTA.EXISTENTE").param("password", PASSWORD).param("passwordConfirmation", PASSWORD))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("userForm", "username"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(PASSWORD))));
        assertThat(users.count()).isEqualTo(count);
        assertThat(users.findByUsername("cuenta.existente").orElseThrow().getPassword()).isEqualTo(originalHash);
    }

    @Test void invalidNamesShortPasswordsAndMismatchedConfirmationDoNotCreateAccounts() throws Exception {
        long count = users.count();
        for (var data : new String[][]{{"nombre con espacios", PASSWORD, PASSWORD}, {"equipo.corto", "corta", "corta"},
                {"equipo.distinto", PASSWORD, "UnaDiferente!2026"}, {"", PASSWORD, PASSWORD}}) {
            mvc.perform(post("/admin/users").with(user("owner").roles("ADMIN")).with(csrf())
                    .param("username", data[0]).param("password", data[1]).param("passwordConfirmation", data[2]))
                    .andExpect(status().isOk()).andExpect(model().hasErrors())
                    .andExpect(view().name("admin/user-form"))
                    .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(data[1]))));
        }
        assertThat(users.count()).isEqualTo(count);
    }

    @Test void utf8PasswordAboveBcryptByteLimitReturnsValidationInsteadOfServerError() throws Exception {
        long count = users.count();
        String password = "á".repeat(37);
        mvc.perform(post("/admin/users").with(user("owner").roles("ADMIN")).with(csrf())
                .param("username", "equipo.bytes").param("password", password).param("passwordConfirmation", password))
                .andExpect(status().isOk()).andExpect(model().hasErrors())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("72 bytes")));
        assertThat(users.count()).isEqualTo(count);
    }

    @Test void anonymousAndStaffCannotAccessOrCreateAccounts() throws Exception {
        long count = users.count();
        for (String path : new String[]{"/admin/users", "/admin/users/new"}) {
            mvc.perform(get(path)).andExpect(status().is3xxRedirection());
            mvc.perform(get(path).with(user("staff").roles("STAFF"))).andExpect(status().isForbidden());
        }
        mvc.perform(post("/admin/users").with(csrf()).param("username", "intruso")
                .param("password", PASSWORD).param("passwordConfirmation", PASSWORD)).andExpect(status().is3xxRedirection());
        mvc.perform(post("/admin/users").with(user("staff").roles("STAFF")).with(csrf()).param("username", "intruso")
                .param("password", PASSWORD).param("passwordConfirmation", PASSWORD)).andExpect(status().isForbidden());
        assertThat(users.count()).isEqualTo(count);
    }

    @Test void csrfIsRequiredAndAccountRoleCannotBeSelectedThroughRequestParameters() throws Exception {
        long count = users.count();
        mvc.perform(post("/admin/users").with(user("owner").roles("ADMIN")).param("username", "sin.token")
                .param("password", PASSWORD).param("passwordConfirmation", PASSWORD)).andExpect(status().isForbidden());
        assertThat(users.count()).isEqualTo(count);
        mvc.perform(post("/admin/users").with(user("owner").roles("ADMIN")).with(csrf()).param("username", "rol.controlado")
                .param("password", PASSWORD).param("passwordConfirmation", PASSWORD).param("role", "STAFF").param("enabled", "false"))
                .andExpect(status().is3xxRedirection());
        var created = users.findByUsername("rol.controlado").orElseThrow();
        assertThat(created.getRole()).isEqualTo(Role.ADMIN);
        assertThat(created.isEnabled()).isTrue();
    }
}
