package com.deluxeterno;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties="app.demo.available=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DisabledDemoTests {
 @Autowired MockMvc mvc;
 @Test void configurationCanDisableDemoEntirely() throws Exception {
  mvc.perform(get("/admin").with(user("admin").roles("ADMIN"))).andExpect(status().isOk()).andExpect(content().string(not(containsString("/admin/demo"))));
  mvc.perform(post("/admin/demo/toggle").with(user("admin").roles("ADMIN")).with(csrf()).param("enabled","true")).andExpect(status().isNotFound());
  mvc.perform(get("/admin/demo").with(user("admin").roles("ADMIN"))).andExpect(status().isNotFound());
 }
}
