package com.deluxeterno;

import com.deluxeterno.web.DemoViewAdvice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DemoModeTests {
 @Autowired MockMvc mvc;
 @Test void activationIsExplicitAndScopedToAdministratorSession() throws Exception {
  var session=new MockHttpSession();
  mvc.perform(get("/admin/products/new").session(session).with(user("presenter").roles("ADMIN")))
   .andExpect(status().isOk()).andExpect(content().string(not(containsString("data-demo-kind"))));
  mvc.perform(post("/admin/demo/toggle").session(session).with(user("presenter").roles("ADMIN")).with(csrf()).param("enabled","true"))
   .andExpect(redirectedUrl("/admin/demo"));
  assertThat(session.getAttribute(DemoViewAdvice.SESSION_KEY)).isEqualTo(true);
  for(var path:new String[]{"/admin/products/new","/admin/categories/new","/contacto"})
   mvc.perform(get(path).session(session).with(user("presenter").roles("ADMIN")))
    .andExpect(status().isOk()).andExpect(content().string(containsString("data-demo-kind"))).andExpect(content().string(containsString("/js/demo.js")));
  mvc.perform(get("/contacto").session(new MockHttpSession())).andExpect(content().string(not(containsString("data-demo-kind"))));
  mvc.perform(post("/admin/demo/toggle").session(session).with(user("presenter").roles("ADMIN")).with(csrf()).param("enabled","false")).andExpect(status().is3xxRedirection());
  assertThat(session.getAttribute(DemoViewAdvice.SESSION_KEY)).isNull();
  mvc.perform(get("/contacto").session(session).with(user("presenter").roles("ADMIN"))).andExpect(content().string(not(containsString("/js/demo.js"))));
 }
 @Test void demoDoesNotBypassRolesOrCsrfOrBecomePublic() throws Exception {
  mvc.perform(post("/admin/demo/toggle").with(user("staff").roles("STAFF")).with(csrf()).param("enabled","true")).andExpect(status().isForbidden());
  mvc.perform(post("/admin/demo/toggle").with(user("admin").roles("ADMIN")).param("enabled","true")).andExpect(status().isForbidden());
  mvc.perform(get("/admin/demo")).andExpect(status().is3xxRedirection());
  var forged=new MockHttpSession();forged.setAttribute(DemoViewAdvice.SESSION_KEY,true);
  mvc.perform(get("/contacto").session(forged)).andExpect(content().string(not(containsString("data-demo-kind"))));
 }
 @Test void logoutInvalidatesDemoSession() throws Exception {
  var session=new MockHttpSession();session.setAttribute(DemoViewAdvice.SESSION_KEY,true);
  mvc.perform(post("/logout").session(session).with(user("admin").roles("ADMIN")).with(csrf())).andExpect(status().is3xxRedirection());
  assertThat(session.isInvalid()).isTrue();
 }
 @Test void demoNeverSkipsProductValidation() throws Exception {
  var session=new MockHttpSession();session.setAttribute(DemoViewAdvice.SESSION_KEY,true);
  mvc.perform(post("/admin/products").session(session).with(user("admin").roles("ADMIN")).with(csrf()).param("name",""))
   .andExpect(status().isOk()).andExpect(model().hasErrors());
 }
}
