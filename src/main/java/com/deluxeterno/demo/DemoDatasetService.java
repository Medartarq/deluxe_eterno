package com.deluxeterno.demo;

import com.deluxeterno.domain.*;
import com.deluxeterno.repository.*;
import com.deluxeterno.service.StockBalance;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.Properties;

@Service
@Profile("demo")
public class DemoDatasetService {
    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
    public static final String VERSION = "2026-10-v1";
    private final ProductRepository products;
    private final CategoryRepository categories;
    private final InquiryRepository inquiries;
    private final AdminUserRepository users;
    private final StockMovementRepository movements;
    private final JdbcTemplate jdbc;
    private final DataSource source;
    private final PasswordEncoder encoder;
    private final String username, password;

    public DemoDatasetService(ProductRepository products, CategoryRepository categories, InquiryRepository inquiries,
            AdminUserRepository users, StockMovementRepository movements, JdbcTemplate jdbc, DataSource source,
            PasswordEncoder encoder, @Value("${app.demo.admin-username}") String username,
            @Value("${app.demo.admin-password}") String password) {
        this.products=products;this.categories=categories;this.inquiries=inquiries;this.users=users;
        this.movements=movements;this.jdbc=jdbc;this.source=source;this.encoder=encoder;
        this.username=username;this.password=password;
    }

    @Transactional
    public void initialize() {
        DemoDatabaseIdentity.verify(source);
        String version=jdbc.queryForObject("select dataset_version from demo_environment where id=1 for update",String.class);
        if (VERSION.equals(version)) return;
        if (version!=null) throw new IllegalStateException("La versión del conjunto demo cambió. Usa el restablecimiento explícito.");
        if (products.count()>0 || categories.count()>0 || inquiries.count()>0 || users.count()>0 || movements.count()>0)
            throw new IllegalStateException("La base demo contiene registros sin versión. No se sobrescriben: utiliza Reset si deseas restablecerlos.");
        seed();
    }

    @Transactional
    public void reset() {
        DemoDatabaseIdentity.verify(source);
        jdbc.queryForObject("select id from demo_environment where id=1 for update",Integer.class);
        entityManager.clear();
        // Solo DML: el borrado y la precarga se confirman o revierten en una misma transacción.
        for (String table : new String[]{"stock_movements","inquiries","product_images","products","categories","admin_users"})
            jdbc.update("delete from "+table);
        seed();
    }

    private void seed() {
        if (username.isBlank() || username.length()>80 || password.length()<12)
            throw new IllegalStateException("Configura un usuario demo y una contraseña de al menos doce caracteres.");
        users.saveAndFlush(new AdminUser(username,encoder.encode(password),Role.ADMIN));
        Properties data=new Properties();
        try (var reader=new InputStreamReader(new ClassPathResource("catalog/products.properties").getInputStream(),StandardCharsets.UTF_8)) {
            data.load(reader);
        } catch (IOException exception) { throw new IllegalStateException("No se pudo cargar el catálogo demo.",exception); }
        for (String key:data.getProperty("products").split(",")) {
            String categoryKey=data.getProperty(key+".category"),categoryName=data.getProperty("category."+categoryKey+".name");
            Category category=categories.findByName(categoryName).orElseGet(()->categories.saveAndFlush(new Category(categoryName,data.getProperty("category."+categoryKey+".description"))));
            Product product=new Product(data.getProperty(key+".name"),data.getProperty(key+".description"),data.getProperty(key+".image"),category);
            product.setCatalogKey("DEMO-"+key);product.setSku("DEMO-"+key);
            product.setSecondaryImagePath(data.getProperty(key+".secondaryImage"));
            product.setPublicationStatus(PublicationStatus.PUBLISHED);product.setSupplyMode(SupplyMode.MADE_TO_ORDER);
            product.setPriceMode(PriceMode.CONSULT);product.setLowStockThreshold(3);
            product.setIncludedItems("Composición de referencia mostrada en la fotografía.\nTarjeta con dedicatoria a coordinar.");
            product.setPersonalization("Nombre, dedicatoria y colores previa coordinación.");
            product.setOccasions("Cumpleaños, aniversario y agradecimiento");
            product.setPreparationTime("Ejemplo: coordinar con 24 a 48 horas de anticipación");
            product.setInternalNotes("[DEMO] Datos ficticios de precio y disponibilidad para este entorno independiente.");
            product.setUpdatedBy(username);
            if (key.equals("DE-004")) product.setPublicationStatus(PublicationStatus.DRAFT);
            if (key.equals("DE-005")) product.setPublicationStatus(PublicationStatus.ARCHIVED);
            if (key.equals("DE-007")) product.setTemporarilyUnavailable(true);
            if (key.equals("DE-006")) {product.setPriceMode(PriceMode.FROM);product.setPrice(new BigDecimal("95.00"));}
            if (key.equals("DE-001") || key.equals("DE-002") || key.equals("DE-003")) {
                product.setSupplyMode(SupplyMode.STOCK);product.setPriceMode(key.equals("DE-002")?PriceMode.FROM:PriceMode.FIXED);
                product.setPrice(new BigDecimal(key.equals("DE-001")?"89.90":key.equals("DE-002")?"75.00":"120.00"));
            }
            products.saveAndFlush(product);
            switch(key) {
                case "DE-001" -> {move(product,StockMovementType.IN,10,"Lote preparado");move(product,StockMovementType.RESERVE,2,"Unidades comprometidas");}
                case "DE-002" -> {move(product,StockMovementType.IN,5,"Lote preparado");move(product,StockMovementType.OUT,2,"Entregas confirmadas");move(product,StockMovementType.CORRECTION,2,"Conteo físico confirmado");}
                case "DE-003" -> {move(product,StockMovementType.IN,3,"Lote preparado");move(product,StockMovementType.RESERVE,1,"Reserva confirmada");move(product,StockMovementType.RELEASE,1,"Reserva liberada");move(product,StockMovementType.OUT,3,"Entregas confirmadas");}
            }
        }
        categories.saveAndFlush(new Category("Colecciones de temporada","Categoría de ejemplo vacía, lista para incorporar productos."));
        String[] messages={"Quisiera personalizar un regalo de aniversario.","¿Qué opciones de flores tienen para cumpleaños?","Quisiera coordinar colores y dedicatoria.","¿Con cuánta anticipación debo solicitar un box?","Gracias, recibí la información de mi consulta.","Confirmo que resolvieron mi consulta sobre preparación."};
        for (int i=0;i<messages.length;i++) {
            Inquiry inquiry=new Inquiry("Cliente de ejemplo "+(i+1),"cliente"+(i+1)+"@example.test",null,"[DEMO] "+messages[i]);
            inquiry.setStatus(i<2?InquiryStatus.NEW:i<4?InquiryStatus.READ:InquiryStatus.RESOLVED);inquiries.save(inquiry);
        }
        products.flush();inquiries.flush();
        jdbc.update("update demo_environment set dataset_version=?,initialized_at=CURRENT_TIMESTAMP where id=1",VERSION);
    }

    private void move(Product product,StockMovementType type,int quantity,String reason) {
        int before=product.getStockQuantity(),reserved=product.getReservedQuantity();
        StockBalance balance=new StockBalance(before,reserved).apply(type,quantity);
        product.setStockQuantity(balance.physical());product.setReservedQuantity(balance.reserved());
        movements.save(new StockMovement(product,type,quantity,before,reserved,"[DEMO] "+reason,username));products.flush();
    }
}
