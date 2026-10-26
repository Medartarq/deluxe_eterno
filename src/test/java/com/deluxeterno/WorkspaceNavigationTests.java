package com.deluxeterno;

import com.deluxeterno.domain.*;
import com.deluxeterno.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WorkspaceNavigationTests {
 @Autowired MockMvc mvc;
 @Autowired ProductRepository products;
 @Autowired CategoryRepository categories;

 @Test void workspaceAccessIsOnlyRenderedForAdministrators() throws Exception {
  for(String page:new String[]{"/","/productos","/galeria","/contacto","/nosotros","/servicios"}) {
   mvc.perform(get(page)).andExpect(status().isOk()).andExpect(content().string(not(containsString("class=\"workspace-toolbar\""))));
   mvc.perform(get(page).with(user("staff").roles("STAFF"))).andExpect(status().isOk()).andExpect(content().string(not(containsString("class=\"workspace-toolbar\""))));
   mvc.perform(get(page).with(user("admin").roles("ADMIN"))).andExpect(status().isOk()).andExpect(content().string(containsString("Ir al panel")));
  }
 }
 @Test void contextActionsLinkToProductEditorAndPreserveCategoryFilter() throws Exception {
  var category=categories.findAll().getFirst();
  var product=products.saveAndFlush(new Product("Navegación de prueba","Detalle","/assets/photo.jpg",category));
  mvc.perform(get("/productos/"+product.getId()).with(user("admin").roles("ADMIN")))
    .andExpect(status().isOk()).andExpect(content().string(containsString("Editar este producto")))
    .andExpect(content().string(containsString("href=\"/admin/products/"+product.getId()+"/edit\"")));
  mvc.perform(get("/productos").param("categoria",category.getId().toString()).with(user("admin").roles("ADMIN")))
    .andExpect(content().string(containsString("href=\"/admin/products?categoryId="+category.getId()+"\"")));
  mvc.perform(get("/admin/products").param("categoryId",category.getId().toString()).with(user("admin").roles("ADMIN")))
    .andExpect(model().attribute("adminPublicUrl","/productos?categoria="+category.getId()));
  mvc.perform(get("/contacto").with(user("admin").roles("ADMIN")))
    .andExpect(content().string(containsString("Ver consultas"))).andExpect(content().string(containsString("href=\"/admin/inquiries\"")));
 }
 @Test void panelReturnsToPublishedProductButNeverLinksToDraftPublicDetail() throws Exception {
  var p=products.saveAndFlush(new Product("Vista pública de prueba","Detalle","/assets/photo.jpg",categories.findAll().getFirst()));
  for(String page:new String[]{"/admin/products/"+p.getId()+"/edit","/admin/inventory/"+p.getId()})
   mvc.perform(get(page).with(user("admin").roles("ADMIN"))).andExpect(status().isOk()).andExpect(model().attribute("adminPublicUrl","/productos/"+p.getId()));
  p.setPublicationStatus(PublicationStatus.DRAFT);products.flush();
  for(String page:new String[]{"/admin/products/"+p.getId()+"/edit","/admin/inventory/"+p.getId()})
   mvc.perform(get(page).with(user("admin").roles("ADMIN"))).andExpect(status().isOk()).andExpect(model().attribute("adminPublicUrl","/productos"));
  mvc.perform(get("/productos/"+p.getId())).andExpect(status().isNotFound());
 }
 @Test void linksRespectApplicationContextPath() throws Exception {
  mvc.perform(get("/deluxe/contacto").contextPath("/deluxe").with(user("admin").roles("ADMIN")))
   .andExpect(status().isOk()).andExpect(content().string(containsString("href=\"/deluxe/admin\"")))
   .andExpect(content().string(containsString("href=\"/deluxe/admin/inquiries\"")));
 }
}
