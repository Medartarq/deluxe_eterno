package com.deluxeterno;

import com.deluxeterno.domain.*;
import com.deluxeterno.dto.*;
import com.deluxeterno.repository.*;
import com.deluxeterno.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties="app.media.directory=target/test-media")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductManagementTests {
 @Autowired MockMvc mvc;
 @Autowired CatalogService catalog;
 @Autowired InventoryService inventory;
 @Autowired ProductRepository products;
 @Autowired CategoryRepository categories;
 @Autowired StockMovementRepository movements;

 ProductForm form() { var f=new ProductForm(); f.setName("Detalle de prueba"); f.setCategoryId(categories.findAll().getFirst().getId()); f.setPublicationStatus(PublicationStatus.DRAFT); return f; }
 @Test void editorAndInventoryViewsRenderForAdministrator() throws Exception {
  var p=catalog.createProduct(form());
  for(var path:new String[]{"/admin/products","/admin/products/new","/admin/products/"+p.getId()+"/edit","/admin/inventory","/admin/inventory/"+p.getId()})
   mvc.perform(get(path).with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
 }
 @Test void draftsStayPrivateAndCannotPublishWithoutImageAndDescription() throws Exception {
  var p=catalog.createProduct(form()); assertThat(p.isActive()).isFalse();
  mvc.perform(get("/productos/"+p.getId())).andExpect(status().isNotFound());
  mvc.perform(post("/admin/products").with(user("admin").roles("ADMIN")).with(csrf()).param("name","Incompleto").param("categoryId",p.getCategory().getId().toString()).param("publicationStatus","PUBLISHED"))
   .andExpect(status().isOk()).andExpect(model().hasErrors());
  assertThatThrownBy(()->catalog.activateProduct(p.getId())).isInstanceOf(ProductEditException.class);
 }
 @Test void galleryAndPresentationPersistWithoutExposingInternalNotes() throws Exception {
  var f=form();f.setDescription("Descripción pública");f.setImagePath("/assets/one.jpg");f.setGallerySubmitted(true);f.setAdditionalImagePaths(java.util.List.of("/assets/two.jpg"));f.setPublicationStatus(PublicationStatus.PUBLISHED);f.setInternalNotes("SECRETO_INTERNO");f.setIncludedItems("Flores y tarjeta");
  var p=catalog.createProduct(f); products.flush();
  assertThat(catalog.findProduct(p.getId()).getGalleryImages()).containsExactly("/assets/one.jpg","/assets/two.jpg");
  mvc.perform(get("/productos/"+p.getId())).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Flores y tarjeta"))).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("SECRETO_INTERNO"))));
 }
 @Test void movementsRecordActorBalancesAndRejectStaleSubmission() {
  var f=form();f.setSupplyMode(SupplyMode.STOCK);var p=catalog.createProduct(f);products.flush();long old=p.getVersion();
  var a=new StockAdjustmentForm();a.setType(StockMovementType.IN);a.setQuantity(10);a.setReason("Ingreso inicial");a.setVersion(old);
  inventory.adjust(p.getId(),a,"operador");
  assertThat(p.getAvailableQuantity()).isEqualTo(10);assertThat(movements.findAll()).anyMatch(m->m.getActor().equals("operador") && m.getPhysicalAfter()==10);
  assertThatThrownBy(()->inventory.adjust(p.getId(),a,"operador")).isInstanceOf(IllegalArgumentException.class);
 }
 @Test void stockFormAndDuplicationKeepOriginalBalance() throws Exception {
  var f=form();f.setSupplyMode(SupplyMode.STOCK);var p=catalog.createProduct(f);p.setStockQuantity(5);p.setReservedQuantity(2);products.flush();
  mvc.perform(get("/admin/inventory/"+p.getId()).with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
  mvc.perform(get("/admin").with(user("admin").roles("ADMIN"))).andExpect(status().isOk()).andExpect(model().attribute("draftCount",1L)).andExpect(model().attribute("lowStockCount",1L));
  mvc.perform(post("/admin/products/"+p.getId()+"/duplicate").with(user("admin").roles("ADMIN")).with(csrf())).andExpect(status().is3xxRedirection());
  var copy=products.findAll().stream().filter(x->x.getName().equals("Copia de Detalle de prueba")).findFirst().orElseThrow();
  assertThat(copy.getPublicationStatus()).isEqualTo(PublicationStatus.DRAFT);assertThat(copy.getStockQuantity()).isZero();assertThat(copy.getReservedQuantity()).isZero();assertThat(copy.getSku()).isNotEqualTo(p.getSku());
  assertThat(p.getStockQuantity()).isEqualTo(5);
  f.setSupplyMode(SupplyMode.MADE_TO_ORDER);f.setVersion(p.getVersion());
  assertThatThrownBy(()->catalog.updateProduct(p.getId(),f)).isInstanceOf(ProductEditException.class);
 }
 @Test void outdatedEditorCannotOverwriteLatestProduct() {
  var f=form();var p=catalog.createProduct(f);products.flush();f.setVersion(p.getVersion()-1);f.setName("Edición antigua");
  assertThatThrownBy(()->catalog.updateProduct(p.getId(),f)).isInstanceOf(ProductEditException.class);
  assertThat(p.getName()).isEqualTo("Detalle de prueba");
 }
 @Test void balanceRulesPreventNegativeStockOrOverReservation() {
  var b=new StockBalance(10,4);
  assertThat(b.apply(StockMovementType.OUT,6)).isEqualTo(new StockBalance(4,4));
  assertThatThrownBy(()->b.apply(StockMovementType.OUT,7)).isInstanceOf(IllegalArgumentException.class);
  assertThatThrownBy(()->b.apply(StockMovementType.RESERVE,7)).isInstanceOf(IllegalArgumentException.class);
  assertThatThrownBy(()->b.apply(StockMovementType.RELEASE,5)).isInstanceOf(IllegalArgumentException.class);
  assertThatThrownBy(()->b.apply(StockMovementType.CORRECTION,3)).isInstanceOf(IllegalArgumentException.class);
  assertThat(new StockBalance(2,0).apply(StockMovementType.CORRECTION,0).physical()).isZero();
 }
 @Test void uploadValidatesDecodedImageAndAccess() throws Exception {
  var bytes=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB),"png",bytes);
  var result=mvc.perform(multipart("/admin/media").file(new MockMultipartFile("file","photo.png","image/png",bytes.toByteArray())).with(user("admin").roles("ADMIN")).with(csrf())).andExpect(status().isCreated()).andReturn();
  String url=new tools.jackson.databind.ObjectMapper().readTree(result.getResponse().getContentAsString()).get("url").asText();
  mvc.perform(get(url)).andExpect(status().isOk()).andExpect(content().contentType("image/png"));
  mvc.perform(multipart("/admin/media").file(new MockMultipartFile("file","bad.png","image/png","<script>bad</script>".getBytes())).with(user("admin").roles("ADMIN")).with(csrf())).andExpect(status().isBadRequest());
  mvc.perform(multipart("/admin/media").file(new MockMultipartFile("file","photo.png","image/png",bytes.toByteArray())).with(user("staff").roles("STAFF")).with(csrf())).andExpect(status().isForbidden());
  mvc.perform(post("/admin/products/1/duplicate").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
 }
}

