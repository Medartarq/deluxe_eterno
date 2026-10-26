package com.deluxeterno;

import com.deluxeterno.demo.*;
import com.deluxeterno.domain.*;
import com.deluxeterno.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={
 "spring.datasource.url=jdbc:h2:mem:deluxe_eterno_demo;MODE=MySQL;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'classpath:demo-marker.sql'",
 "app.demo.admin-username=admin-demo", "app.demo.admin-password=DemoIntegration!2026"
})
@AutoConfigureMockMvc
@ActiveProfiles({"demo","test"})
@Transactional
class IsolatedDemoTests {
 @Autowired DemoDatasetService dataset;
 @Autowired ProductRepository products;
 @Autowired CategoryRepository categories;
 @Autowired InquiryRepository inquiries;
 @Autowired StockMovementRepository movements;
 @Autowired AdminUserRepository users;
 @Autowired PasswordEncoder encoder;
 @Autowired MockMvc mvc;

 @Test void scenariosAndBalancesAreCompleteAndCoherent() throws Exception {
  assertThat(products.count()).isEqualTo(35);assertThat(categories.count()).isEqualTo(8);
  assertThat(inquiries.countByStatus(InquiryStatus.NEW)).isEqualTo(2);
  assertThat(inquiries.countByStatus(InquiryStatus.READ)).isEqualTo(2);
  assertThat(inquiries.countByStatus(InquiryStatus.RESOLVED)).isEqualTo(2);
  assertThat(movements.count()).isEqualTo(9);
  var available=products.findByCatalogKey("DEMO-DE-001").orElseThrow();
  assertThat(available.getStockQuantity()).isEqualTo(10);assertThat(available.getReservedQuantity()).isEqualTo(2);assertThat(available.getAvailableQuantity()).isEqualTo(8);
  assertThat(products.findByCatalogKey("DEMO-DE-002").orElseThrow().isLowStock()).isTrue();
  assertThat(products.findByCatalogKey("DEMO-DE-003").orElseThrow().getAvailabilityLabel()).isEqualTo("Agotado");
  assertThat(products.findByCatalogKey("DEMO-DE-004").orElseThrow().getPublicationStatus()).isEqualTo(PublicationStatus.DRAFT);
  assertThat(products.findByCatalogKey("DEMO-DE-005").orElseThrow().getPublicationStatus()).isEqualTo(PublicationStatus.ARCHIVED);
  assertThat(products.findByCatalogKey("DEMO-DE-007").orElseThrow().isTemporarilyUnavailable()).isTrue();
  assertThat(products.findByCatalogKey("DEMO-DE-016").orElseThrow().getGalleryImages()).hasSize(2);
  var admin=users.findByUsername("admin-demo").orElseThrow();assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
  assertThat(encoder.matches("DemoIntegration!2026",admin.getPassword())).isTrue();
  mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(containsString("Entorno demo")));
  mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(content().string(containsString("Entorno demo")));
 }
 @Test void initializationNeverDuplicatesOrOverwritesEditsOnRestart() {
  var p=products.findByCatalogKey("DEMO-DE-001").orElseThrow();p.setName("Edición conservada");products.flush();Long id=p.getId();
  dataset.initialize();
  assertThat(products.count()).isEqualTo(35);assertThat(movements.count()).isEqualTo(9);
  assertThat(products.findByCatalogKey("DEMO-DE-001").orElseThrow().getName()).isEqualTo("Edición conservada");
  assertThat(products.findByCatalogKey("DEMO-DE-001").orElseThrow().getId()).isEqualTo(id);
 }
 @Test void explicitResetRestoresDatasetAndRemovesExtraDemoRows() {
  var p=products.findByCatalogKey("DEMO-DE-001").orElseThrow();p.setName("Edición descartable");products.flush();
  categories.saveAndFlush(new Category("Categoría descartable","Demo"));
  dataset.reset();
  assertThat(products.count()).isEqualTo(35);assertThat(categories.count()).isEqualTo(8);assertThat(inquiries.count()).isEqualTo(6);assertThat(movements.count()).isEqualTo(9);assertThat(users.count()).isEqualTo(1);
  assertThat(products.findByCatalogKey("DEMO-DE-001").orElseThrow().getName()).isEqualTo("Rayito de Ternura");
 }
}
