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
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Flujo HTTP completo: los ejemplos utilizan los mismos controladores que los datos normales. */
@SpringBootTest(properties="app.media.directory=target/test-media")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExhibitionIntegrationTests {
 @Autowired MockMvc mvc;
 @Autowired ProductRepository products;
 @Autowired CategoryRepository categories;
 @Autowired InquiryRepository inquiries;
 @Autowired StockMovementRepository movements;

 @Test void categoryProductStockPublicationAndInquiryWorkTogether() throws Exception {
  mvc.perform(post("/admin/categories").with(user("expositor").roles("ADMIN")).with(csrf())
    .param("name","DEMO Categoría integrada").param("description","Categoría de exposición"))
    .andExpect(redirectedUrl("/admin/categories"));
  var category=categories.findAll().stream().filter(c->c.getName().equals("DEMO Categoría integrada")).findFirst().orElseThrow();
  var photoBytes=new java.io.ByteArrayOutputStream();
  javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(80,100,java.awt.image.BufferedImage.TYPE_INT_RGB),"png",photoBytes);
  var upload=mvc.perform(multipart("/admin/media").file(new org.springframework.mock.web.MockMultipartFile("file","demo.png","image/png",photoBytes.toByteArray()))
      .with(user("expositor").roles("ADMIN")).with(csrf())).andExpect(status().isCreated()).andReturn();
  String photo=new tools.jackson.databind.ObjectMapper().readTree(upload.getResponse().getContentAsString()).get("url").asText();
  mvc.perform(get(photo)).andExpect(status().isOk()).andExpect(content().contentType("image/png"));
  mvc.perform(post("/admin/products").with(user("expositor").roles("ADMIN")).with(csrf())
    .param("name","DEMO Producto integrado").param("categoryId",category.getId().toString())
    .param("description","Regalo de exposición").param("imagePath",photo)
    .param("publicationStatus","DRAFT").param("supplyMode","STOCK").param("priceMode","FIXED").param("price","89.90"))
    .andExpect(redirectedUrl("/admin/products"));
  var product=products.findAll().stream().filter(p->p.getName().equals("DEMO Producto integrado")).findFirst().orElseThrow();
  mvc.perform(get("/productos/"+product.getId())).andExpect(status().isNotFound());
  mvc.perform(post("/admin/products/"+product.getId()+"/activate").with(user("expositor").roles("ADMIN")).with(csrf())).andExpect(status().is3xxRedirection());
  mvc.perform(get("/productos").param("categoria",category.getId().toString()))
    .andExpect(content().string(containsString("DEMO Producto integrado"))).andExpect(content().string(containsString("Agotado")));
  move(product,"IN",10); move(product,"RESERVE",2); move(product,"RELEASE",2); move(product,"OUT",2);
  assertThat(product.getStockQuantity()).isEqualTo(8);assertThat(product.getReservedQuantity()).isZero();
  assertThat(movements.findAll().stream().filter(m->m.getProduct().getId().equals(product.getId()))).hasSize(4);
  mvc.perform(get("/productos/"+product.getId())).andExpect(status().isOk()).andExpect(content().string(containsString("Disponible"))).andExpect(content().string(containsString("89.90"))).andExpect(content().string(containsString(photo)));
  mvc.perform(get("/galeria").param("categoria",category.getId().toString())).andExpect(content().string(containsString("DEMO Producto integrado")));
  mvc.perform(post("/contactos").with(csrf()).param("name","DEMO Cliente integrado").param("email","exposicion@example.test").param("message","[DEMO] Quisiera personalizar el regalo."))
    .andExpect(redirectedUrl("/contacto"));
  var inquiry=inquiries.findAll().stream().filter(q->q.getName().equals("DEMO Cliente integrado")).findFirst().orElseThrow();
  assertThat(product.getAvailableQuantity()).isEqualTo(8);
  mvc.perform(get("/admin/inquiries").with(user("expositor").roles("ADMIN"))).andExpect(content().string(containsString("DEMO Cliente integrado")));
  for(String state:new String[]{"READ","RESOLVED"})
   mvc.perform(post("/admin/inquiries/"+inquiry.getId()+"/status").with(user("expositor").roles("ADMIN")).with(csrf()).param("status",state)).andExpect(status().is3xxRedirection());
  assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.RESOLVED);
  mvc.perform(post("/admin/categories/"+category.getId()+"/delete").with(user("expositor").roles("ADMIN")).with(csrf())).andExpect(flash().attributeExists("errorMessage"));
  assertThat(categories.existsById(category.getId())).isTrue();
  mvc.perform(post("/admin/products/"+product.getId()+"/delete").with(user("expositor").roles("ADMIN")).with(csrf())).andExpect(status().is3xxRedirection());
  mvc.perform(get("/productos/"+product.getId())).andExpect(status().isNotFound());
  mvc.perform(get("/galeria").param("categoria",category.getId().toString())).andExpect(content().string(not(containsString("DEMO Producto integrado"))));
  assertThat(product.getStockQuantity()).isEqualTo(8);
  mvc.perform(post("/admin/products/"+product.getId()+"/activate").with(user("expositor").roles("ADMIN")).with(csrf())).andExpect(status().is3xxRedirection());
  mvc.perform(get("/productos/"+product.getId())).andExpect(status().isOk());
 }
 private void move(Product product,String type,int quantity) throws Exception {
  products.flush();
  mvc.perform(post("/admin/inventory/"+product.getId()).with(user("expositor").roles("ADMIN")).with(csrf())
    .param("type",type).param("quantity",Integer.toString(quantity)).param("version",Long.toString(product.getVersion())).param("reason","[DEMO] Integración "+type))
    .andExpect(redirectedUrl("/admin/inventory/"+product.getId()));
 }
}
