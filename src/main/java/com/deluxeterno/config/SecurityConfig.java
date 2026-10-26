package com.deluxeterno.config;

import com.deluxeterno.domain.AdminUser;
import com.deluxeterno.repository.AdminUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/",
                                "/nosotros",
                                "/productos",
                                "/productos/**",
                                "/servicios",
                                "/galeria",
                                "/contacto",
                                "/contactos",
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/assets/**",
                                "/favicon.ico",
                                "/error")
                        .permitAll()
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/admin", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(AdminUserRepository adminUserRepository) {
        return username -> {
            AdminUser adminUser = adminUserRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
            return User.withUsername(adminUser.getUsername())
                    .password(adminUser.getPassword())
                    .roles(adminUser.getRole().name())
                    .disabled(!adminUser.isEnabled())
                    .build();
        };
    }
}
