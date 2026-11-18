package com.deluxeterno;

import com.deluxeterno.repository.InquiryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InquiryAbuseProtectionTests {
    @Autowired MockMvc mvc;
    @Autowired InquiryRepository inquiries;

    @Test
    void honeypotSubmissionIsNotPersisted() throws Exception {
        long before = inquiries.count();

        mvc.perform(inquiry("198.51.100.10").param("website", "https://bot.example"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/contact"))
                .andExpect(model().hasErrors());

        assertThat(inquiries.count()).isEqualTo(before);
    }

    @Test
    void sixthSubmissionFromTheSameAddressIsRejected() throws Exception {
        long before = inquiries.count();
        String address = "198.51.100.20";

        for (int attempt = 0; attempt < 5; attempt++) {
            mvc.perform(inquiry(address))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/contacto"));
        }
        mvc.perform(inquiry(address))
                .andExpect(status().isOk())
                .andExpect(view().name("public/contact"))
                .andExpect(model().hasErrors());

        assertThat(inquiries.count()).isEqualTo(before + 5);
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder inquiry(String address) {
        return post("/contactos")
                .with(csrf())
                .with(request -> {
                    request.setRemoteAddr(address);
                    return request;
                })
                .param("name", "Cliente de prueba")
                .param("email", "cliente@example.com")
                .param("message", "Consulta de prueba");
    }
}
