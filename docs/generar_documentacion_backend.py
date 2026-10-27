from pathlib import Path
import re, hashlib

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/Backend_completo_Deluxe_Eterno.txt'
JAVA=ROOT/'src/main/java/com/deluxeterno'
parts=[]
def add(t):parts.append(t.strip()+'\n')
add('''BACKEND COMPLETO DE DELUXE ETERNO
Explicación técnica, rutas HTTP, funciones y archivos relativos
Fecha de documentación: 26 de octubre de 2026

Todas las rutas de archivos de este documento son relativas a la raíz del
repositorio deluxe-eterno. Una ruta HTTP, como /admin/products, es una dirección
que atiende el servidor; una ruta de archivo, como
src/main/java/com/deluxeterno/web/admin/ProductAdminController.java, identifica
el código que implementa esa función. Son conceptos diferentes.

Este documento contiene dos partes: una guía explicada de la implementación
y un anexo con el código Java completo del backend y sus configuraciones.
El anexo permite consultar los cuerpos completos de los métodos, incluidos
constructores, getters, setters y auxiliares, sin confundir un resumen con
la implementación. No incluye archivos .env ni credenciales locales.

CONTENIDO
01 Arquitectura y dependencias
02 Arranque de la aplicación y perfiles
03 Mapa completo de rutas HTTP
04 Controladores públicos y administrativos
05 Servicios y reglas de negocio
06 DTO y validación de entrada
07 Entidades, tablas y enumeraciones
08 Repositorios y consultas
09 Seguridad, sesiones y navegación entre espacios
10 Imágenes y errores HTTP
11 Entorno demo y autollenado
12 Configuración y ejecución local
13 Flujos completos de información
14 Pruebas, límites y decisiones actuales
15 Índice de archivos Java
16 Anexo de código completo y configuración
''')
add('''01 ARQUITECTURA Y DEPENDENCIAS
=============================

El sistema es una aplicación monolítica Java con Spring Boot. Su interfaz
principal se renderiza en el servidor con Thymeleaf. No es una SPA y no se
implementa una API REST general para todos los recursos.

Flujo habitual de una lectura:
Navegador -> SecurityFilterChain -> controlador MVC -> servicio -> repositorio
-> Hibernate/JDBC -> MySQL -> Model -> Thymeleaf -> HTML para el navegador.

Responsabilidades por paquete:
  web/          Rutas públicas, medios y atributos compartidos de las vistas.
  web/admin/    Controladores del panel de administración.
  service/      Operaciones del dominio y reglas de negocio.
  dto/          Datos aceptados desde formularios y sus restricciones.
  domain/       Entidades JPA, enumeraciones y estados.
  repository/   Interfaces de acceso a la base de datos.
  config/       Seguridad e inicializadores del entorno principal.
  demo/         Verificación, precarga y restablecimiento de la base demo.

Archivos de presentación relacionados:
  src/main/resources/templates/public/       Vistas del sitio público.
  src/main/resources/templates/admin/        Vistas administrativas.
  src/main/resources/templates/auth/login.html
  src/main/resources/templates/fragments/    Estructuras reutilizables.
  src/main/resources/templates/error/        Plantillas 404 y 500.
  src/main/resources/static/                 CSS, JavaScript e imágenes.

Dependencias declaradas en pom.xml:
  Java 26: versión de compilación configurada.
  Spring Boot 4.1.1: parent, autoconfiguración y ciclo de arranque.
  spring-boot-starter-web: Spring MVC, HTTP, multipart y servidor integrado.
  spring-boot-starter-thymeleaf: integración de plantillas y formularios.
  spring-boot-starter-data-jpa: repositorios, Hibernate y persistencia JPA.
  spring-boot-starter-security: autenticación, roles, sesión, CSRF y BCrypt.
  spring-boot-starter-validation: Bean Validation con anotaciones Jakarta.
  mysql-connector-j: driver JDBC de ejecución para MySQL.
  h2: base en memoria, exclusivamente con alcance de pruebas Maven.
  spring-boot-starter-test, spring-boot-webmvc-test y spring-security-test:
    soporte de pruebas, MockMvc, seguridad simulada y aserciones.
  spring-boot-maven-plugin: ejecución y empaquetado de la aplicación.

Bootstrap 5.3.3 e Icons 1.11.3 pertenecen al frontend y se cargan por CDN.
La carga de archivos devuelve JSON mediante los conversores del soporte web.
ImageIO, BufferedImage, UUID, NIO e Instant son APIs del JDK, no dependencias
adicionales declaradas en Maven.

Spring construye los beans y los inyecta principalmente mediante constructores.
@Controller coordina vistas; @RestController devuelve cuerpos HTTP.
@Service define servicios; JpaRepository aporta acceso a datos; @Entity mapea
tablas; @Transactional delimita las unidades de trabajo.
''')
add('''02 ARRANQUE DE LA APLICACIÓN Y PERFILES
======================================

Archivo: src/main/java/com/deluxeterno/DeluxeEternoApplication.java
Función: main(String[] args).
Inicia SpringApplication. La exploración de componentes encuentra las clases
del paquete com.deluxeterno y sus subpaquetes. La autoconfiguración prepara el
servidor, la seguridad, las vistas y el acceso a la base según las dependencias.

Archivo: src/main/java/com/deluxeterno/config/AdminUserInitializer.java
Función: run(String... args).
Se ejecuta al arrancar en perfiles distintos de demo. Si app.admin.password
está vacía o admin_users ya contiene algún registro, no crea otro usuario.
Si la tabla está vacía y la contraseña está definida, guarda un AdminUser
con rol ADMIN y contraseña codificada mediante PasswordEncoder/BCrypt.
Cambiar ADMIN_PASSWORD posteriormente no resetea una cuenta existente.

Archivo: src/main/java/com/deluxeterno/config/CatalogDataInitializer.java
Función: run(String... args).
En perfiles distintos de demo lee, como UTF-8, el recurso
src/main/resources/catalog/products.properties. Recorre sus claves de producto;
si existsByCatalogKey indica que una ya existe, la omite. Si no existe, busca
o crea la categoría y guarda el producto con su fotografía principal y, si
corresponde, una secundaria. La clave estable evita duplicar o sobrescribir
ediciones del administrador al reiniciar. No borra registros ajenos a la carga.

No debe asumirse un orden explícito entre ambos inicializadores: el código
no declara @Order. Tampoco se deben editar los inicializadores para registrar
contraseñas personales en Git.

Con el perfil demo, estos dos inicializadores se excluyen por @Profile("!demo")
y entran los componentes del paquete demo, descritos en el apartado 11.
El perfil test configura H2 y evita depender del MySQL principal para la suite.
''')

routes=[
('GET','/','PublicController.home','public/index','Público'),
('GET','/nosotros','PublicController.about','public/about','Público'),
('GET','/productos','PublicController.products','public/products','Público; categoria opcional'),
('GET','/productos/{id}','PublicController.productDetail','public/product-detail','Público; solo activo'),
('GET','/servicios','PublicController.services','public/services','Público'),
('GET','/galeria','PublicController.gallery','public/gallery','Público; categoria opcional'),
('GET','/contacto','PublicController.contact','public/contact','Público'),
('POST','/contactos','PublicController.submitInquiry','Error en vista o redirect:/contacto','Público; CSRF'),
('GET','/login','PublicController.login','auth/login','Público'),
('POST','/login','Filtros de Spring Security','Login y redirección /admin','Credenciales y CSRF'),
('POST','/logout','Filtros de Spring Security','Invalidar sesión y redirigir /','Sesión y CSRF'),
('GET','/admin','AdminController.dashboard','admin/dashboard','ADMIN'),
('GET','/admin/categories','AdminController.categories','admin/categories','ADMIN'),
('GET','/admin/categories/new','AdminController.newCategory','admin/category-form','ADMIN'),
('POST','/admin/categories','AdminController.createCategory','Guardar o devolver formulario','ADMIN; CSRF'),
('GET','/admin/categories/{id}/edit','AdminController.editCategory','admin/category-form','ADMIN'),
('POST','/admin/categories/{id}/edit','AdminController.updateCategory','Actualizar o devolver formulario','ADMIN; CSRF'),
('POST','/admin/categories/{id}/delete','AdminController.deleteCategory','Eliminar si no tiene productos','ADMIN; CSRF'),
('GET','/admin/inquiries','AdminController.inquiries','admin/inquiries','ADMIN'),
('POST','/admin/inquiries/{id}/status','AdminController.updateInquiryStatus','Actualizar estado y redirigir','ADMIN; CSRF'),
('GET','/admin/products','ProductAdminController.list','admin/products','ADMIN; q categoryId status mode'),
('GET','/admin/products/new','ProductAdminController.createForm','admin/product-form','ADMIN'),
('POST','/admin/products','ProductAdminController.create','Crear producto y redirigir','ADMIN; CSRF'),
('GET','/admin/products/{id}/edit','ProductAdminController.editForm','admin/product-form','ADMIN'),
('POST','/admin/products/{id}/edit','ProductAdminController.update','Editar producto y redirigir','ADMIN; CSRF'),
('POST','/admin/products/{id}/delete','ProductAdminController.archive','Archivado lógico','ADMIN; CSRF'),
('POST','/admin/products/{id}/activate','ProductAdminController.publish','Publicar producto','ADMIN; CSRF'),
('POST','/admin/products/{id}/duplicate','ProductAdminController.duplicate','Crear copia y abrir edición','ADMIN; CSRF'),
('GET','/admin/inventory','InventoryController.list','admin/inventory','ADMIN; q low'),
('GET','/admin/inventory/{id}','InventoryController.detail','admin/stock-detail','ADMIN; page'),
('POST','/admin/inventory/{id}','InventoryController.adjust','Registrar movimiento y redirigir','ADMIN; CSRF'),
('GET','/admin/demo','DemoController.guide','admin/demo','ADMIN; available=true'),
('POST','/admin/demo/toggle','DemoController.toggle','Preferencia de autollenado en sesión','ADMIN; CSRF; enabled'),
('POST','/admin/media','MediaController.upload','201 JSON con url','ADMIN; CSRF; multipart file'),
('GET','/media/{filename}','MediaController.read','Archivo JPG o PNG','Público')]
add('03 MAPA COMPLETO DE RUTAS HTTP\n=============================\n\n'+
    '\n\n'.join(f'{i:02d}. {method} {url}\n    Función: {fn}\n    Respuesta: {view}\n    Acceso y parámetros: {access}' for i,(method,url,fn,view,access) in enumerate(routes,1)))
add('''Las rutas de recursos /css/**, /js/**, /assets/** y /favicon.ico son
públicas y normalmente las resuelve el soporte de recursos estáticos de Spring.
/error pertenece al tratamiento de errores de Spring Boot, no a un método
propio de PublicController. SecurityConfig también la permite públicamente.
GET /logout puede mostrar la confirmación gestionada por Security cuando
corresponda; el cierre efectivo de sesión protegido se realiza mediante POST.
Los parámetros entre llaves son variables de ruta, no nombres de carpetas.
No hay rutas implementadas para registro público, CRUD de usuarios, pedidos,
pagos, eliminación física de productos o restablecimiento de demo por HTTP.
''')
add('''04 CONTROLADORES PÚBLICOS Y ADMINISTRATIVOS
==========================================

4.1 src/main/java/com/deluxeterno/web/PublicController.java

Constructor: recibe CatalogService e InquiryService.
home(Model): agrega categories y products destacados; devuelve public/index.
about(): devuelve la vista informativa public/about, sin consulta de negocio.
products(Long categoryId, Model): obtiene categorías públicas y productos
  activos, opcionalmente filtrados por categoria; agrega selectedCategory.
productDetail(Long id, Model): recupera un producto público activo y agrega
  product al modelo. El servicio lanza 404 si no lo encuentra.
services(): devuelve public/services, cuyo contenido editorial está en HTML.
gallery(Long categoryId, Model): reutiliza categorías y productos activos;
  prepara products, categories y selectedCategory para public/gallery.
login(): devuelve auth/login; NO verifica por sí mismo las credenciales.
contact(Model): crea InquiryForm si el modelo todavía no contiene uno;
  permite conservar datos y errores cuando corresponde.
submitInquiry(InquiryForm, BindingResult, RedirectAttributes): @Valid valida
  el formulario. Ante errores devuelve public/contact; si es válido llama
  inquiryService.create, agrega successMessage y redirige a /contacto.

4.2 src/main/java/com/deluxeterno/web/admin/AdminController.java

Constructor: recibe CatalogService e InquiryService.
dashboard(Model): agrega newInquiryCount, totalInquiryCount, draftCount,
  lowStockCount, productCount y categoryCount. No calcula ventas o ingresos.
categories(Model): carga todas las categorías para administración.
newCategory(Model): entrega CategoryForm vacío.
createCategory(CategoryForm, BindingResult, RedirectAttributes): valida,
  crea y redirige; transforma DuplicateCategoryException en error de name.
editCategory(Long id, Model): busca Category y copia nombre/descripción a
  CategoryForm; agrega categoryId y editing=true.
updateCategory(Long id, CategoryForm, BindingResult, Model, RedirectAttributes):
  conserva contexto de edición en caso de error; actualiza y redirige si es válido.
deleteCategory(Long id, RedirectAttributes): pide borrado al servicio; captura
  asociación con productos o conflicto de integridad y muestra mensaje flash.
inquiries(Model): carga consultas ordenadas por fecha descendente.
updateInquiryStatus(Long id, InquiryStatus, RedirectAttributes): cambia el estado
  recibido y redirige a /admin/inquiries. No envía una respuesta por correo.

4.3 src/main/java/com/deluxeterno/web/admin/ProductAdminController.java

Constructor: recibe CatalogService.
list(q, categoryId, status, mode, Model): obtiene todos los productos y filtra
  con streams Java por nombre/SKU, categoría, publicación y modalidad. Los
  filtros de esta pantalla no se implementan como una consulta paginada SQL.
  Agrega productos, total, published, drafts, lowStock y opciones de los filtros.
createForm(Model): establece DRAFT y gallerySubmitted=true en un ProductForm.
editForm(id, Model): transforma el producto existente mediante toForm.
create(form, errors, model, redirect, principal): valida DTO y categoría,
  llama createProduct con principal.getName() y muestra confirmación.
update(id, form, errors, model, redirect, principal): valida y actualiza;
  ProductEditException regresa al formulario como error global.
archive(id, redirect): llama deactivateProduct. La ruta dice /delete pero
  conserva el registro e historial; es un archivado, no DELETE SQL.
publish(id, redirect): llama activateProduct y comunica requisitos pendientes.
duplicate(id, redirect, principal): obtiene el formulario del producto original,
  antepone "Copia de ", limita el nombre a 120, establece DRAFT y version=null,
  crea otro producto y abre su edición. No copia físico ni reservado.

Métodos privados:
validateCategory(form, errors): evita aceptar una categoría que dejó de existir.
options(Model): agrega categorías y valores de los enums de publicación,
  suministro y precio.
prepare(Model, id): agrega opciones, edición, ID, SKU, enlace a tienda y una
  biblioteca de hasta 100 productos con imagen para reutilizar fotografías.
toForm(Product): copia los datos editables, versión, enfoque y fotografías;
  la primera imagen se trata como principal y el resto como adicionales.

4.4 src/main/java/com/deluxeterno/web/admin/InventoryController.java

Constructor: recibe CatalogService e InventoryService.
list(q, low, Model): filtra nombre/SKU en memoria; si low=true conserva
  únicamente p.isLowStock(). Devuelve admin/inventory.
detail(id, page, Model): prepara StockAdjustmentForm con la versión vigente
  del producto y llama populate. page vale cero si no se proporciona.
adjust(id, form, errors, model, redirect, principal): valida el DTO, llama
  inventory.adjust con el responsable autenticado y captura reglas inválidas.
  Si hay errores recarga saldos e historial; si no, redirige al detalle.
populate(Model, id, page): agrega product, movements, movementTypes y el enlace
  público, que apunta al detalle solo si el producto está activo.

4.5 src/main/java/com/deluxeterno/web/admin/DemoController.java

Constructor: obtiene app.demo.available.
guide(): verifica disponibilidad y devuelve admin/demo.
toggle(boolean enabled, HttpSession): crea o elimina el atributo de sesión
  deluxeDemoEnabled y redirige a /admin/demo. No cambia el DataSource.
checkAvailable(): devuelve 404 mediante ResponseStatusException si la ayuda
  está deshabilitada en configuración. El rol ADMIN lo exige SecurityConfig.

4.6 src/main/java/com/deluxeterno/web/MediaController.java

Constructor: recibe MediaStorageService.
upload(MultipartFile file): recibe el campo multipart "file", llama store
  y devuelve HTTP 201 con un mapa JSON {"url":"/media/archivo..."}.
read(String filename): entrega Resource con Content-Type JPG o PNG y
  Cache-Control de una hora.
invalidImage(IllegalArgumentException): manejador local que devuelve 400
  con JSON {"error":"mensaje"}. No es una ruta HTTP independiente.
''')
add('''05 SERVICIOS Y REGLAS DE NEGOCIO
===============================

5.1 src/main/java/com/deluxeterno/service/CatalogService.java

La clase usa @Transactional(readOnly=true). Sus métodos de escritura declaran
@Transactional para permitir cambios dentro de una unidad de trabajo.
Constructor: recibe CategoryRepository y ProductRepository.

Consultas:
findCategories(): categorías con al menos un producto activo, sin duplicados.
findAllCategories(): todas las categorías administrativas, ordenadas por nombre.
findActiveProducts(categoryId): selecciona todos los productos activos o los
  activos de una categoría. Se ordenan por nombre.
findAllProducts(): lista administrativa completa por nombre.
findFeaturedProducts(): primeros tres activos por nombre; no usa ventas.
countDrafts(): cuenta publicationStatus=DRAFT.
countLowStock(): cuenta productos STOCK cuyo disponible está bajo el umbral.
countProducts(): total de registros, incluidos los que no se muestran públicamente.
countCategories(): total de categorías.
categoryExists(id): verifica existencia para validar el formulario.
findProduct(id): solo busca activo. Si no existe, 404. Inicializa la colección
  additionalImagePaths dentro de la transacción antes de devolver el producto.
findAnyProduct(id): consulta sin filtro público; 404 si no existe e inicializa
  fotografías adicionales para su uso fuera de la transacción.
findCategory(id): obtiene categoría o genera 404.

Altas y edición:
createProduct(form): sobrecarga que registra actor "sistema".
createProduct(form, actor): construye Product con su categoría, genera SKU
  DE- más 12 caracteres de UUID, aplica el formulario y guarda con save.
updateProduct(id, form): sobrecarga con actor "sistema".
updateProduct(id, form, actor): obtiene findForUpdate; si el formulario incluye
  versión y no coincide, lanza ProductEditException. Impide pasar de STOCK a
  MADE_TO_ORDER si quedan unidades físicas o reservadas. Actualiza identidad,
  descripción, foto, categoría y el resto de atributos y guarda.
applyForm(product, form, actor): auxiliar privado; recorta textos principales,
  aplica estados y modalidades; guarda precio=null si es CONSULT; copia
  composición, personalización, ocasiones, preparación, notas internas,
  indisponibilidad, umbral, responsable y enfoque. Si gallerySubmitted=true,
  limpia la fotografía secundaria heredada y reemplaza adicionales, eliminando
  duplicados y la ruta principal de la lista adicional.

Publicación:
deactivateProduct(id): busca el producto y llama setActive(false), que lo
  sincroniza como ARCHIVED. La entidad gestionada se actualiza al confirmar.
activateProduct(id): exige descripción e imagen principal no vacías;
  setActive(true) lo publica. El producto puede estar publicado y agotado:
  publicación y disponibilidad son dimensiones distintas.

Categorías:
createCategory(form): trim del nombre, búsqueda duplicada ignoreCase y guardado.
updateCategory(id, form): comprueba duplicados excluyendo el ID actual.
saveCategory(category): auxiliar que usa saveAndFlush; convierte una carrera
  de integridad en DuplicateCategoryException para el formulario.
deleteCategory(id): prohíbe borrado si existe cualquier producto asociado,
  incluso archivado; en caso contrario elimina físicamente la categoría.

5.2 src/main/java/com/deluxeterno/service/InventoryService.java

Constructor: recibe ProductRepository y StockMovementRepository.
adjust(id, form, actor), transaccional:
  1 Recupera el producto con bloqueo PESSIMISTIC_WRITE o devuelve 404.
  2 Exige supplyMode=STOCK.
  3 Exige versión recibida no nula e igual a la actual.
  4 Exige motivo no vacío, máximo 255 caracteres.
  5 Conserva los saldos anteriores.
  6 Construye StockBalance y aplica el movimiento.
  7 Asigna físico y reservado resultantes y updatedBy.
  8 Rechaza un ajuste que no modifica las cantidades.
  9 Guarda StockMovement con tipo, cantidad, motivo y actor.
 10 Ejecuta products.flush para sincronizar los cambios.
El saldo y el historial pertenecen a la misma transacción. Un fallo no debe
confirmar uno sin el otro. El bloqueo serializa la escritura del producto y la
comparación de versión detecta formularios abiertos con un saldo anterior.

history(id, page), lectura: obtiene movimientos ordenados por fecha e ID
descendentes, 20 por página, y corrige números de página negativos a cero.

5.3 src/main/java/com/deluxeterno/service/StockBalance.java

Es un record inmutable con physical y reserved.
Su constructor compacto rechaza físico o reservado negativos, reservado mayor
que físico y físico superior a 1 000 000.
apply(type, quantity) valida tipo y cantidad entre cero y un millón. Solo
CORRECTION acepta cero; los otros movimientos requieren cantidad positiva.
  IN: physical + quantity, mismas reservas.
  OUT: physical - quantity, mismas reservas.
  RESERVE: mismo físico, reserved + quantity.
  RELEASE: mismo físico, reserved - quantity.
  CORRECTION: physical = quantity, mismas reservas.
Las sumas usan Math.addExact y cada resultado pasa de nuevo por el constructor.
Una salida no libera reservas implícitamente. La corrección es un saldo final,
no una diferencia que se suma. Una reserva es una operación administrativa,
no una compra ni una consecuencia automática de enviar una consulta.

5.4 src/main/java/com/deluxeterno/service/InquiryService.java

Constructor: recibe InquiryRepository. Clase de lectura por defecto.
create(InquiryForm): transacción de escritura; crea Inquiry con datos del
  remitente y mensaje, estado inicial NEW y fecha actual.
findAll(): consultas por fecha descendente.
countNew(): cuenta NEW.
countAll(): total de consultas.
updateStatus(id, status): transacción; busca o genera 404 y cambia el estado.
  Hibernate detecta el cambio en la entidad gestionada aunque no se escriba
  explícitamente save otra vez. No hay envío de correo o mensaje externo.

5.5 src/main/java/com/deluxeterno/service/MediaStorageService.java

Constructor(directory): obtiene app.media.directory, convierte el directorio
  en Path absoluto y normalizado. Por defecto utiliza ./uploads.
store(MultipartFile):
  - Rechaza archivo vacío o de más de 5 * 1024 * 1024 bytes.
  - Detecta un lector ImageIO y exige formato real JPEG o PNG.
  - Comprueba mínimo de 64 píxeles por lado y máximo de 24 megapíxeles.
  - Decodifica y reduce proporcionalmente el lado mayor a 1600, sin ampliar.
  - Utiliza interpolación bicúbica y transparencia para PNG.
  - Crea el directorio si falta y un nombre UUID con extensión controlada.
  - Recodifica para guardar solo la imagen procesada y devuelve /media/UUID.ext.
  - Convierte fallos de procesamiento o escritura en IllegalArgumentException.
load(filename): acepta únicamente UUID con .jpg o .png; resuelve y normaliza
  la ruta, comprueba que pertenezca al directorio y que sea archivo regular.
  Si falla, 404; si existe, devuelve PathResource.

5.6 Excepciones de negocio
src/main/java/com/deluxeterno/service/ProductEditException.java
  Constructor(message): RuntimeException para conflictos de edición/publicación.
src/main/java/com/deluxeterno/service/DuplicateCategoryException.java
  Constructor(): RuntimeException con el mensaje de categoría duplicada.
Los controladores convierten estas excepciones concretas en mensajes de usuario;
no debe suponerse que cualquier excepción Java está capturada por esos bloques.
''')
add('''06 DTO Y VALIDACIÓN DE ENTRADA
=============================

Un DTO recibe datos HTTP y define lo que puede editar el usuario. No reemplaza
las restricciones de la base ni las reglas transaccionales de los servicios.
@Valid activa validación y BindingResult contiene errores para volver a la vista.

src/main/java/com/deluxeterno/dto/InquiryForm.java
  name: @NotBlank, @Size(max=120).
  email: @NotBlank, @Email, @Size(max=180).
  phone: opcional, @Size(max=40).
  message: @NotBlank, @Size(max=2000).
  Getters y setters permiten el binding y el renderizado de errores.

src/main/java/com/deluxeterno/dto/CategoryForm.java
  name: @NotBlank, máximo 80.
  description: opcional, máximo 255.
  La unicidad se comprueba en CatalogService y se refuerza en MySQL.

src/main/java/com/deluxeterno/dto/ProductForm.java
  name: obligatorio, máximo 120.
  description: máximo 1000; puede estar vacía en borrador.
  imagePath: máximo 255; vacía o ruta local/URL http(s) con patrón permitido.
  categoryId: obligatorio.
  publicationStatus, supplyMode, priceMode: no nulos.
  price: si existe, mínimo 0.01, hasta 8 dígitos enteros y 2 decimales.
  includedItems: máximo 1000.
  personalization: máximo 500.
  occasions: máximo 160.
  preparationTime: máximo 120.
  internalNotes: máximo 1000; uso administrativo.
  temporarilyUnavailable: indicador booleano.
  lowStockThreshold: entre 0 y 1 000 000.
  version: versión recibida del editor, contrastada por el servicio si existe.
  gallerySubmitted: indica reemplazo explícito de la lista de fotografías.
  focusX, focusY: entre 0 y 100; valores iniciales de enfoque 50.
  additionalImagePaths: máximo 4, cada ruta hasta 255 y patrón permitido.

  isReadyToPublish(), @AssertTrue:
    Si el estado es PUBLISHED, exige descripción y fotografía principal.
  isPriceConsistent(), @AssertTrue:
    Si el precio no es CONSULT, exige un importe.
  Los getters/setters restantes transportan campos, no ejecutan SQL.
  La carga HTTP de imágenes es una operación distinta: una ruta que pasa el
  patrón del DTO no implica que se haya verificado el contenido de una imagen
  remota. ImageIO valida los archivos enviados a /admin/media.

src/main/java/com/deluxeterno/dto/StockAdjustmentForm.java
  type: @NotNull.
  quantity: entre 0 y 1 000 000; StockBalance limita cuándo se admite cero.
  reason: obligatorio, máximo 255.
  version: @NotNull.
  Los getters/setters enlazan el formulario con InventoryController.

La validación HTML required/maxlength ofrece ayuda inmediata, pero el backend
vuelve a validar. JavaScript de autollenado no omite @Valid ni las reglas.
''')
add('''07 ENTIDADES TABLAS Y ENUMERACIONES
==================================

Todos los archivos de este apartado están en
src/main/java/com/deluxeterno/domain/.

AdminUser.java -> admin_users
  id autoincremental; username único hasta 80; password hasta 255 para el hash;
  role enumerado y enabled. Constructor protegido para JPA y constructor con
  usuario, hash y rol. Getters exponen esos datos a seguridad. No hay controlador
  de alta/edición de usuarios en la aplicación actual.

Category.java -> categories
  id, name único y obligatorio hasta 80 y description hasta 255.
  Constructor JPA, constructor(name, description), getters y setters.
  El borrado está controlado por CatalogService y por la FK desde products.

Product.java -> products
  id, name, description, imagePath, secondaryImagePath, catalogKey y sku.
  Estados publicationStatus, supplyMode, priceMode e importe BigDecimal.
  includedItems, personalization, occasions, preparationTime e internalNotes.
  temporarilyUnavailable, stockQuantity, reservedQuantity, lowStockThreshold.
  focusX/focusY, version, updatedAt, updatedBy y active.
  category: @ManyToOne LAZY obligatorio mediante category_id.
  additionalImagePaths: @ElementCollection, tabla product_images con product_id,
  image_order e image_path. No existe una entidad Java ProductImage.

  Funciones con lógica propia:
  setActive(boolean): sincroniza active y el estado PUBLISHED/ARCHIVED.
  getSku(): devuelve sku; si falta usa catalogKey; si falta también, DE-id.
  getPublicationStatus(): permite compatibilidad con registros heredados sin
    valor de estado, a partir de active.
  setPublicationStatus(value): sincroniza active con PUBLISHED.
  getSupplyMode()/getPriceMode(): ofrecen valores por defecto para datos
    antiguos sin modalidad (MADE_TO_ORDER y CONSULT).
  getAvailableQuantity(): stockQuantity - reservedQuantity.
  isLowStock(): solo STOCK y disponible <= lowStockThreshold.
  getGalleryImages(): LinkedHashSet con principal, secundaria heredada y
    adicionales; elimina duplicados manteniendo orden.
  getAvailabilityLabel(): prioridad a indisponibilidad temporal; después
    "Por encargo", "Agotado", "Últimas unidades" o "Disponible".
  getPriceLabel(): "Precio a consultar" o importe en soles, con "Desde" si aplica.
  touch(): callback @PrePersist/@PreUpdate; registra Instant.now() en updatedAt.
  Los demás getters/setters exponen o asignan campos. No todos los setters
  validan individualmente las invariantes: DTO y servicios son parte del contrato.

Inquiry.java -> inquiries
  id, name, email, phone, message, status y createdAt.
  Constructor de negocio inicializa fecha y estado NEW.
  Getters y setStatus permiten registro y seguimiento. No hay relación JPA
  con producto, pedido o administrador.

StockMovement.java -> stock_movements
  id; product con @ManyToOne; type; quantity; physicalBefore/physicalAfter;
  reservedBefore/reservedAfter; reason; actor y createdAt.
  El constructor recibe el producto ya actualizado, toma los saldos finales
  de él y conserva los anteriores proporcionados por el servicio.
  Los getters presentan el historial. actor es texto, no FK hacia admin_users.

Enumeraciones:
  Role.java: ADMIN y STAFF. El panel actual solo permite ADMIN.
  InquiryStatus.java: NEW, READ, RESOLVED.
  PublicationStatus.java: DRAFT, PUBLISHED, ARCHIVED y getLabel().
  SupplyMode.java: MADE_TO_ORDER, STOCK y getLabel().
  PriceMode.java: CONSULT, FIXED, FROM y getLabel().
  StockMovementType.java: IN, OUT, RESERVE, RELEASE, CORRECTION y getLabel().
Las etiquetas en español son presentación; los nombres enum son los valores
de programa usados en el binding y la persistencia.

Relaciones principales:
  categories 1 -> N products.
  products 1 -> N product_images.
  products 1 -> N stock_movements.
  inquiries y admin_users no tienen FK entre sí ni con products.
  La tabla demo_environment existe solo como marcador del entorno demo.
''')
add('''08 REPOSITORIOS Y CONSULTAS
==========================

Directorio: src/main/java/com/deluxeterno/repository/.
Las interfaces extienden JpaRepository: Spring genera la implementación.
No hay una clase escrita a mano por cada método CRUD. Las operaciones heredadas
incluyen save, saveAndFlush, findById, existsById, findAll, count, delete y flush.
save crea o integra entidades según su estado; las actualizaciones de entidades
gestionadas también pueden persistirse al confirmar una transacción.

AdminUserRepository.java
  findByUsername(username): Optional usado por UserDetailsService.

CategoryRepository.java
  findAllByOrderByNameAsc(): listado administrativo.
  findByName(name): categoría de la importación.
  existsByNameIgnoreCase(name): detectar duplicados al crear.
  existsByNameIgnoreCaseAndIdNot(name,id): duplicados al editar.
  findPublishedCategories(): JPQL select distinct p.category desde productos
    activos, por nombre. No devuelve categorías vacías al catálogo público.

ProductRepository.java
  findAllByActiveTrueOrderByNameAsc(): catálogo público completo.
  findTop3ByActiveTrueOrderByNameAsc(): destacados.
  findByActiveTrueAndCategory_IdOrderByNameAsc(categoryId): filtro público.
  findAllByOrderByNameAsc(): todos los productos para administración.
  findByIdAndActiveTrue(id): detalle público.
  Los cinco métodos anteriores usan @EntityGraph(attributePaths="category")
    para obtener la categoría junto con el resultado y reducir consultas extra.
  existsByCategory_Id(id): impide borrar categorías con productos.
  existsByCatalogKey(key): importación idempotente.
  findByCatalogKey(key): búsqueda por clave de importación.
  countByPublicationStatus(status): indicadores de borradores.
  countLowStock(mode): JPQL con stockQuantity - reservedQuantity <= umbral.
  findForUpdate(id): JPQL por ID y @Lock(PESSIMISTIC_WRITE), dentro de las
    transacciones que editan producto o inventario.

InquiryRepository.java
  findAllByOrderByCreatedAtDesc(): bandeja de consultas.
  countByStatus(status): indicador de consultas nuevas.

StockMovementRepository.java
  findByProduct_IdOrderByCreatedAtDescIdDesc(productId, Pageable): Page con
    historial del producto. page y tamaño se definen en InventoryService.

JPA no implica que todos los filtros se ejecuten en SQL: algunos listados
administrativos cargan registros y luego filtran mediante streams Java.
open-in-view=false evita prolongar el contexto de persistencia durante la vista;
por ello CatalogService inicializa fotografías adicionales antes de salir.
''')
add('''09 SEGURIDAD SESIONES Y NAVEGACIÓN ENTRE ESPACIOS
===============================================

src/main/java/com/deluxeterno/config/SecurityConfig.java

securityFilterChain(HttpSecurity):
  Permite rutas públicas, recursos, /media/** y /error.
  Exige hasRole("ADMIN") para /admin/**.
  Cualquier otra ruta exige autenticación, sin convertirla en ruta existente.
  Configura /login como vista de acceso y /admin como destino forzado al entrar.
  Logout redirige a /, invalida sesión y pide borrar JSESSIONID.
  CSRF no está deshabilitado: protege las operaciones POST.
passwordEncoder(): bean BCryptPasswordEncoder.
userDetailsService(AdminUserRepository): devuelve una función que busca por
  username; si falta lanza UsernameNotFoundException. Construye UserDetails
  con hash, rol y estado disabled según enabled. Spring añade el prefijo ROLE_
  cuando utiliza roles(...), de modo que ADMIN corresponde a ROLE_ADMIN.

La autenticación reside en HttpSession y una cookie de sesión. La cookie no
contiene el catálogo ni equivale a una base de datos del navegador.
No hay JWT, registro público, recuperación de contraseña por correo o interfaz
de creación de usuarios adicionales. El administrador inicial se explica en 02.
La configuración demo cambia el nombre de cookie a DELUXE_DEMO_SESSION;
la invalidación de sesión es la que revoca la autenticación al salir.

src/main/java/com/deluxeterno/web/WorkspaceNavigationAdvice.java
  Access: record con administrator, managementUrl y managementLabel.
  access(HttpServletRequest): @ModelAttribute workspaceAccess global.
    Si no hay ADMIN, devuelve false y enlaces nulos.
    En /productos/ID, enlace a editar ese producto.
    En /, /productos y /galeria, enlace a administrar catálogo; valida que
      categoria sea un entero positivo acotado antes de transferir el filtro.
    En /contacto, enlace a consultas administrativas.
    En otras páginas, mantiene administrador sin acción contextual.
  Los enlaces se derivan de rutas conocidas, no de una URL Referer arbitraria.
  Esta ayuda de navegación no reemplaza el filtro de autorización.

src/main/java/com/deluxeterno/web/admin/AdminViewAdvice.java
  Se aplica solo al paquete web.admin.
  username(Principal): atributo adminUsername, o "Administrador" si no hay principal.
  publicUrl(request): atributo adminPublicUrl. Productos e inventario vuelven
    a /productos y transfieren categoryId válido como categoria; categorías
    vuelven a /#categorias; consultas a /contacto; lo demás a /.
  section(request): atributo adminSection; identifica products, inventory,
    categories, inquiries, demo o dashboard según la ruta.
  Algunos controladores reemplazan adminPublicUrl con el detalle público
  específico si conocen un producto activo.
''')
add('''10 IMÁGENES Y ERRORES HTTP
=========================

El transporte HTTP y el almacenamiento están separados:
  MediaController recibe MultipartFile -> MediaStorageService procesa disco
  -> JSON con URL -> ProductForm recibe la ruta -> Product conserva la ruta.
Subir una imagen no guarda por sí solo un producto. Si se abandona el formulario
después de subirla, puede quedar un archivo sin uso; no se implementa una tarea
automática de limpieza de archivos huérfanos.

src/main/java/com/deluxeterno/web/UploadExceptionHandler.java
  tooLarge(): @ExceptionHandler(MaxUploadSizeExceededException.class).
  Es @RestControllerAdvice y devuelve HTTP 413 con JSON {error:...}.
  No tiene una URL propia: se invoca al manejar esa excepción de multipart.

Respuestas relevantes:
  200: vista o recurso encontrado; también formulario devuelto con errores.
  201: imagen subida correctamente.
  3xx: login, PRG y otras redirecciones.
  400: imagen rechazada por el servicio/manejador de MediaController;
       ciertos errores de binding HTTP también pueden producir 400.
  403: rol insuficiente o token CSRF ausente/inválido según Security.
  404: producto/categoría/consulta/medio inexistente o acceso demo deshabilitado.
  413: límite general multipart superado.
  500: errores no controlados; se dispone de plantilla de error correspondiente.

No todo error de negocio devuelve 400: los controladores MVC suelen devolver
el formulario con mensajes y código 200. No se debe afirmar que las plantillas
404/500 equivalen a un manejador global personalizado de todas las excepciones.

Límites de tamaño: la configuración servlet admite 6 MB por archivo y 7 MB por
solicitud; MediaStorageService admite hasta 5 MB por fotografía. Un archivo
que supera 5 MB sin superar el límite multipart se rechaza en el servicio.
''')
add('''11 ENTORNO DEMO Y AUTOLLENADO
===========================

Son mecanismos separados:
  Entorno demo: otra instancia/perfil con MySQL y archivos independientes.
  Autollenado: estado por sesión que permite completar ejemplos en formularios.

src/main/java/com/deluxeterno/web/DemoViewAdvice.java
  SESSION_KEY = "deluxeDemoEnabled".
  Constructor: lee app.demo.available y app.demo.isolated.
  isolated(): atributo demoEnvironment, indica aislamiento de la instancia.
  available(): atributo demoAvailable, indica disponibilidad de la ayuda.
  enabled(request): atributo demoEnabled. Solo true si está disponible, el
    usuario es ADMIN y una sesión existente tiene SESSION_KEY=true.
  El autollenado utiliza static/js/demo.js y fragments/demo.html. Completa
    campos y dispara eventos input/change; guardar exige la acción normal.
  Activarlo en una instancia principal no la cambia a la base demo.

src/main/java/com/deluxeterno/demo/DemoDatabaseIdentity.java
  SCHEMA = deluxe_eterno_demo; APP_KEY = deluxe-eterno-demo.
  verify(DataSource): abre conexión y delega; SQL fallido se comunica como
    IllegalStateException, con indicación de inicializar el entorno.
  verify(Connection): exige connection.getCatalog() igual al esquema permitido
    y consulta demo_environment(id=1) para comprobar app_key. Si no coincide,
    cancela la operación. No basta con cambiar el nombre del perfil.

src/main/java/com/deluxeterno/demo/DemoDatabaseGuard.java
  Activo solo con @Profile("demo").
  verifyDemoDataSourceBeforeHibernate(): bean estático BeanPostProcessor.
  postProcessAfterInitialization(bean, name): si bean es DataSource, verifica
    su identidad antes de que Hibernate lo use para modificar tablas.

src/main/java/com/deluxeterno/demo/DemoDataInitializer.java
  Constructor: recibe DemoDatasetService, banderas reset/exit, directorio
    de medios y contexto de aplicación.
  run(String...): exige que el directorio termine en uploads-demo; impide
    reset sin exit. Llama reset o initialize. Si exit=true, termina con
    SpringApplication.exit y System.exit para una ejecución local de una vez.
  Solo existe bajo el perfil demo.

src/main/java/com/deluxeterno/demo/DemoDatasetService.java
  Constructor: recibe cinco repositorios, JdbcTemplate, DataSource,
    PasswordEncoder y credenciales demo de configuración.
  VERSION = "2026-10-v1".
  initialize(), transaccional: verifica identidad, bloquea el marcador con
    SELECT ... FOR UPDATE y lee dataset_version. Si ya coincide, no repite
    la carga. Si existe otra versión o datos sin versión, no los sobrescribe.
    Si corresponde una instalación inicial vacía, llama seed().
  reset(), transaccional: verifica identidad y bloquea el marcador; limpia
    EntityManager y borra mediante DML, en orden, stock_movements, inquiries,
    product_images, products, categories y admin_users; después llama seed().
    Borrado y precarga se confirman o revierten conjuntamente.
  seed(), privado: exige usuario no vacío de hasta 80 y contraseña de al menos
    12 caracteres; crea administrador con BCrypt; carga products.properties;
    usa claves DEMO-, prepara datos comerciales y modifica escenarios:
      DE-004: borrador; DE-005: archivado; DE-007: indisponibilidad temporal.
      DE-006: precio desde S/ 95.00.
      DE-001: STOCK fijo S/ 89.90, entrada 10 y reserva 2.
      DE-002: STOCK desde S/ 75.00, entrada 5, salida 2, corrección física a 2.
      DE-003: STOCK fijo S/ 120.00, entrada 3, reserva 1, liberación 1 y salida 3.
    Crea además una categoría vacía y seis consultas ficticias: dos NEW,
    dos READ y dos RESOLVED. Actualiza versión y fecha de inicialización.
  move(product, type, quantity, reason), privado: aplica StockBalance,
    asigna saldos, guarda StockMovement con prefijo DEMO y hace flush.
  Estos precios y plazos son ejemplos del conjunto demo, no precios oficiales.

scripts/demo.ps1
  -Action Initialize: comprueba/crea base y marcador, usuario MySQL limitado
    al esquema demo y configuración .env.demo.local ignorada por Git.
  -Action Start: carga configuración y ejecuta Maven con perfil demo.
  -Action Status: consulta identidad y versión del entorno.
  -Action Reset: pide confirmación propia del script y ejecuta una operación
    local de una vez; se utiliza con la instancia demo detenida.
  Funciones de apoyo: Read-DemoSettings, Invoke-LocalMysql, New-DemoPassword
    y Assert-DemoSettings. No hay un endpoint web de reset.
''')
add('''12 CONFIGURACIÓN Y EJECUCIÓN LOCAL
=================================

src/main/resources/application.yml
  spring.datasource.url <- DB_URL o conexión localhost:3306/deluxe_eterno.
  spring.datasource.username <- DB_USERNAME o root.
  spring.datasource.password <- DB_PASSWORD o vacío.
  spring.jpa.hibernate.ddl-auto <- JPA_DDL_AUTO o update.
  spring.jpa.open-in-view=false.
  spring.thymeleaf.cache=false para el trabajo local.
  server.port <- PORT o 8080.
  app.demo.available <- DEMO_AVAILABLE o true.
  app.media.directory <- MEDIA_DIRECTORY o ./uploads.
  app.admin.username <- ADMIN_USERNAME o admin.
  app.admin.password <- ADMIN_PASSWORD o vacío.

src/main/resources/application-demo.yml
  Conexión MySQL deluxe_eterno_demo, usuario deluxe_demo_local por defecto,
  y credenciales obtenidas de DEMO_DB_* y DEMO_ADMIN_*.
  server.address=127.0.0.1; puerto DEMO_PORT o 8081.
  Cookie de sesión DELUXE_DEMO_SESSION; app.demo.isolated=true.
  Directorio ./uploads-demo; reset y salida inicial desactivados por defecto.

src/test/resources/application-test.yml
  H2 en memoria con modo de compatibilidad MySQL; ddl-auto=create-drop.
  No representa la conexión a la base principal ni una copia persistente demo.

Ejemplo de instalación principal en PowerShell, con credenciales de ejemplo:
  $env:DB_USERNAME = "usuario_mysql_local"
  $env:DB_PASSWORD = "clave_mysql_local"
  $env:ADMIN_USERNAME = "admin"
  $env:ADMIN_PASSWORD = "clave_inicial_elegida_localmente"
  mvn spring-boot:run

La base deluxe_eterno debe existir para una instalación normal. Hibernate
ddl-auto=update puede preparar tablas, pero no equivale a una herramienta de
migraciones versionadas. Las variables ADMIN_* solo crean la primera cuenta
si no hay usuarios; importar una base con cuentas conserva sus hashes.
Un archivo .env no se carga automáticamente solo por existir: el script demo
sí lee su archivo local; la ejecución principal necesita variables o una
configuración que las suministre al proceso Java.
''')
add('''13 FLUJOS COMPLETOS DE INFORMACIÓN
=================================

A. Catálogo público
GET /productos?categoria=1 -> PublicController.products ->
CatalogService.findActiveProducts(1) -> ProductRepository -> MySQL -> Model ->
public/products.html. Después catalog.js busca y ordena en el navegador sobre
las tarjetas recibidas, sin SQL por cada letra y conservando q/orden en la URL.

B. Guardar un producto
Formulario admin/product-form.html -> POST /admin/products con token CSRF ->
Security exige ADMIN -> binding a ProductForm -> @Valid -> validateCategory ->
CatalogService.createProduct(form, principal.getName()) -> SKU y applyForm ->
ProductRepository.save -> confirmación transaccional -> mensaje flash ->
redirect:/admin/products. Si es DRAFT, no aparece en las lecturas públicas.

C. Editar un producto
GET edición copia entidad a DTO y entrega version -> POST edición valida ->
findForUpdate bloquea registro -> compara version -> aplica formulario ->
actualiza. La lectura y la edición usan DTO diferentes de la entidad gestionada.
En el servicio, la comparación de edición solo se realiza si form.version no
es nula; la pantalla normalmente envía el valor preparado por toForm.

D. Ajustar stock
GET detalle entrega versión y saldo -> POST ajuste -> DTO -> bloqueo ->
comprobación de versión obligatoria -> StockBalance.apply -> actualización
de Product y alta de StockMovement -> flush y commit -> redirección.
El movimiento conserva saldos anteriores y posteriores y el usuario responsable.

E. Registrar y atender consulta
GET /contacto -> InquiryForm -> POST /contactos -> validación ->
InquiryService.create -> InquiryRepository.save -> NEW -> redirección.
Después ADMIN abre /admin/inquiries y POST /admin/inquiries/ID/status ->
InquiryService.updateStatus -> cambio a READ o RESOLVED -> commit.
No existe reserva de stock o envío de email automático en esta secuencia.

F. Fotografías
La subida multipart devuelve URL JSON; esa URL se coloca en el formulario.
Al guardar el producto, JPA persiste las rutas. Al visitar /media/archivo,
el backend lee el archivo del directorio configurado; no lee un BLOB MySQL.

G. Inicio de sesión
GET /login devuelve HTML -> POST /login pasa por filtros -> UserDetailsService
busca AdminUser -> BCrypt compara la clave con el hash -> sesión autenticada ->
/admin. Los GET públicos pueden seguir utilizándose con la misma sesión y
mostrar los accesos contextuales preparados por los Advice.
''')
add('''14 PRUEBAS LÍMITES Y DECISIONES ACTUALES
=====================================

Directorio: src/test/java/com/deluxeterno/.
DeluxeEternoApplicationTests: arranque del contexto.
AuditRegressionTests: regresiones y reglas verificadas por la auditoría.
ProductCatalogIntegrationTests: catálogo y presentación de productos.
ProductManagementTests: formularios, publicación, duplicación, versión,
  inventario e imágenes.
ExhibitionIntegrationTests: recorrido categoría-producto-stock-publicación-consulta.
WorkspaceNavigationTests: accesos entre tienda y panel según rol y contexto.
DemoModeTests y DisabledDemoTests: autollenado y disponibilidad de la ayuda.
DemoDatabaseGuardTests e IsolatedDemoTests: protección y aislamiento demo.

Las pruebas utilizan MockMvc y H2 con perfil test. La última ejecución
documentada de la suite, durante la actualización del informe, terminó con
59 pruebas, cero fallos y cero errores. Este documento explica el código;
su generación no implica una nueva prueba manual de todas las operaciones.

Límites actuales que conviene explicar con precisión:
  - No existe CRUD web de usuarios adicionales ni cambio de contraseña.
  - Los listados administrativos de productos e inventario filtran en memoria.
  - La búsqueda pública necesita las tarjetas previamente cargadas.
  - La disponibilidad no constituye una reserva comercial automática.
  - El historial pagina resultados; el resto de listas no tiene necesariamente
    la misma estrategia de paginación.
  - No hay carrito, pago, facturación, pedidos o API social integrados.
  - La base y los archivos de imágenes requieren respaldos por separado.
  - Un hash BCrypt no es una contraseña cifrada reversible.
  - El autollenado no aísla datos: el perfil y la conexión demo sí lo hacen.
  - ddl-auto=update es una decisión local; la colaboración puede beneficiarse
    de migraciones versionadas y políticas de datos de prueba.

No se necesita modificar el backend para leer esta documentación. Los archivos
de código se incorporan a continuación como referencia de su estado actual.
''')

files=sorted(JAVA.rglob('*.java'))
add('15 ÍNDICE DE ARCHIVOS JAVA\n=========================\n\n'+
    '\n'.join(f'{i:02d}. {f.relative_to(ROOT).as_posix()}' for i,f in enumerate(files,1)))
add('''16 ANEXO DE CÓDIGO COMPLETO Y CONFIGURACIÓN
=========================================

Cada bloque identifica un archivo relativo y su SHA-256. El contenido Java
se reproduce completo, sin números añadidos a las líneas, para facilitar su
lectura y comparación con el repositorio. Las explicaciones de los apartados
anteriores complementan este código; no son un reemplazo de sus condiciones.
''')
for f in files+[ROOT/'pom.xml',ROOT/'src/main/resources/application.yml',ROOT/'src/main/resources/application-demo.yml',ROOT/'src/test/resources/application-test.yml',ROOT/'scripts/demo.ps1']:
    data=f.read_bytes()
    add('\n'+'='*78+'\nARCHIVO RELATIVO: '+f.relative_to(ROOT).as_posix()+'\nSHA-256: '+hashlib.sha256(data).hexdigest()+'\n'+'='*78+'\n\n'+data.decode('utf-8-sig'))
add('FIN DEL DOCUMENTO')
OUT.write_text('\n'.join(parts),encoding='utf-8-sig')
text=OUT.read_text(encoding='utf-8-sig')
assert len(routes)==35
assert all(str(f.relative_to(ROOT).as_posix()) in text for f in files)
print(f'Archivo: {OUT}\nRutas documentadas: {len(routes)}\nArchivos Java completos: {len(files)}\nLíneas: {len(text.splitlines())}\nBytes: {OUT.stat().st_size}')
