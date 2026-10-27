from pathlib import Path
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs' / 'Guion_tecnico_demostracion_Deluxe_Eterno.docx'
d = Document()
s = d.sections[0]
s.page_height, s.page_width = Inches(11.69), Inches(8.27)
s.top_margin = s.bottom_margin = Inches(.72)
s.left_margin = s.right_margin = Inches(.8)
for name in ['Normal','Title','Subtitle','Heading 1','Heading 2','Heading 3']:
    st=d.styles[name]; st.font.name='Calibri'; st.font.color.rgb=RGBColor(0,0,0)
d.styles['Normal'].font.size=Pt(11)
d.styles['Normal'].paragraph_format.space_after=Pt(7)
d.styles['Normal'].paragraph_format.line_spacing=1.08
d.styles['Title'].font.size=Pt(27)
d.styles['Heading 1'].font.size=Pt(20)
d.styles['Heading 2'].font.size=Pt(12)
for style in d.styles:
    for border in list(style.element.iter(qn('w:pBdr'))):
        border.getparent().remove(border)
footer=s.footer.paragraphs[0]; footer.alignment=2
r=footer.add_run('Deluxe Eterno  |  Guion técnico  |  '); r.font.size=Pt(9)
field=OxmlElement('w:fldSimple'); field.set(qn('w:instr'),'PAGE'); footer._p.append(field)
d.core_properties.title='Guion técnico de demostración de Deluxe Eterno'
d.core_properties.subject='Recorrido funcional y explicación de arquitectura y dependencias'
d.core_properties.author='Equipo Deluxe Eterno'

def p(t): d.add_paragraph(t)
def h(t): d.add_heading(t,2)
def page(title, time=None):
    d.add_page_break(); d.add_heading(title,1)
    if time: p(time)
def block(title,text): h(title); p(text)
def table(headers,rows,widths):
    t=d.add_table(rows=1, cols=len(headers)); t.autofit=False
    for c,w in zip(t.columns,widths): c.width=Inches(w)
    for c,v in zip(t.rows[0].cells,headers): c.text=v
    for row in rows:
        for c,v in zip(t.add_row().cells,row): c.text=v
    for ri,row in enumerate(t.rows):
        for ci,c in enumerate(row.cells):
            c.width=Inches(widths[ci]); c.vertical_alignment=1
            pr=c._tc.get_or_add_tcPr()
            borders=OxmlElement('w:tcBorders')
            for edge in ['top','left','bottom','right']:
                el=OxmlElement('w:'+edge); el.set(qn('w:val'),'single'); el.set(qn('w:sz'),'5'); el.set(qn('w:color'),'D9D9D9'); borders.append(el)
            pr.append(borders)
            sh=OxmlElement('w:shd'); sh.set(qn('w:fill'),'E8EDF2' if ri==0 else ('F6F7F8' if ri%2==0 else 'FFFFFF')); pr.append(sh)
            for par in c.paragraphs:
                par.paragraph_format.space_after=Pt(5); par.paragraph_format.space_before=Pt(5)
                for run in par.runs: run.font.size=Pt(10); run.bold=ri==0
    repeat=OxmlElement('w:tblHeader'); t.rows[0]._tr.get_or_add_trPr().append(repeat)
    d.add_paragraph().paragraph_format.space_after=Pt(0)

d.add_heading('Guion técnico de demostración de Deluxe Eterno',0)
d.add_paragraph('Recorrido funcional y explicación del código',style='Subtitle')
p('Versión del guion 26 de octubre de 2026')
p('Este guion permite demostrar la aplicación y explicar qué componentes intervienen en cada acción. El recorrido conecta el sitio público, la administración, la persistencia y las reglas de negocio. Está organizado para una demostración técnica de 18 a 22 minutos; las notas de código sirven como apoyo para responder preguntas.')
block('Idea que articula la demostración','Deluxe Eterno utiliza una aplicación web monolítica con renderizado en el servidor. Spring MVC recibe las solicitudes, los servicios aplican las reglas, Spring Data JPA accede a los datos y Thymeleaf genera el HTML. JavaScript incorpora interacciones locales sin convertir el sitio en una SPA.')
block('Caso conductor','Crearemos el producto Abrazo de Primavera, lo publicaremos, registraremos sus existencias y atenderemos una consulta. Usaremos el entorno demo para que las operaciones de la demostración permanezcan separadas de la base principal.')
table(['Tramo','Contenido','Tiempo'],[
('Apertura','Arquitectura y tecnologías','2 min'),('Sitio público','Inicio, Nosotros, catálogo, servicios y galería','4 min'),('Contacto y acceso','Consulta, validación y autenticación','3 min'),('Administración','Categorías, producto e imágenes','4 min'),('Inventario e integración','Movimientos, consultas y comprobación pública','4 min'),('Cierre técnico','Demo, pruebas y alcance','3 min')],[1.15,4.3,1.15])
block('Preparación','Abre el sitio y el panel en dos pestañas del mismo entorno demo, cuyo puerto predeterminado es 8081. Comprueba la precarga, la sesión administrativa y una fotografía JPG o PNG de prueba. Verifica la disponibilidad de Bootstrap y Bootstrap Icons servidos por CDN. Las credenciales se obtienen de la configuración local y no se muestran en pantalla.')

page('1 Arquitectura y dependencias','Apoyo para la apertura y las preguntas técnicas')
p('Diálogo sugerido: “La aplicación separa presentación, coordinación HTTP, reglas de negocio y persistencia. Las dependencias declaradas en Maven integran estos componentes y Spring inyecta sus colaboradores mediante constructores”.')
table(['Tecnología','Dónde interviene y para qué'],[
('Java 26','Lenguaje y versión configurada en pom.xml. Modela entidades, DTO, servicios, controladores y reglas como StockBalance.'),
('Spring Boot 4.1.1','Parent Maven y autoconfiguración. Inicia el contexto de beans y el servidor web integrado para ejecutar la aplicación.'),
('starter-web y Spring MVC','Enrutamiento GET y POST, Model, BindingResult, multipart y respuestas HTTP. Los controladores HTML y MediaController utilizan este soporte.'),
('starter-thymeleaf','Renderiza plantillas, fragmentos, atributos th:each, th:field y mensajes de validación. El navegador recibe HTML ya construido.'),
('starter-data-jpa','Integra repositorios, transacciones y Hibernate como proveedor ORM. Traduce entidades y consultas a operaciones sobre la base.'),
('mysql-connector-j','Driver JDBC de ejecución. Comunica el DataSource con MySQL; no genera la interfaz ni define las reglas de stock.'),
('starter-security','Cadena de filtros, autenticación por formulario, sesión, autorización ADMIN, protección CSRF y BCrypt.'),
('starter-validation','Bean Validation con anotaciones Jakarta, como @NotBlank, @Email y @AssertTrue. Verifica los DTO antes de guardar.'),
('Bootstrap 5.3.3 e Icons 1.11.3','Dependencias del navegador por CDN. Rejilla adaptable, componentes y pictogramas, complementados con CSS propio.'),
('JavaScript nativo','Búsqueda y orden del catálogo, encuadres, subida de archivos, autollenado y contador de contacto. No requiere React ni Angular.'),
('Dependencias de prueba','starter-test, webmvc-test y security-test permiten pruebas con JUnit y MockMvc. H2 tiene alcance test; no es la base demo MySQL.')],[1.65,4.95])
p('Maven administra las dependencias directas y transitivas. El parent de Spring Boot coordina sus versiones compatibles y spring-boot-maven-plugin permite ejecutar y empaquetar la aplicación. El archivo pom.xml es el punto de referencia para reproducir la compilación con el equipo.')

page('2 Inicio y Nosotros','Tiempo orientativo 2 minutos')
block('Acción en pantalla','Abre / y recorre las secciones. Muestra los accesos a categorías y productos destacados. Después abre /nosotros y presenta la identidad de la empresa.')
block('Diálogo técnico','“En Inicio, PublicController.home incorpora categorías y productos al Model. CatalogService solicita las categorías publicadas y tres productos activos ordenados por nombre. Thymeleaf recorre esas colecciones y construye las tarjetas antes de enviar la respuesta. Los destacados actuales son una selección por orden alfabético; no se calculan a partir de ventas”.')
block('Qué ejecuta cada componente','Spring MVC resuelve GET /. CatalogService coordina la lectura y ProductRepository y CategoryRepository ejecutan las consultas mediante Spring Data JPA y Hibernate. MySQL almacena los registros. templates/public/index.html presenta el resultado y los fragmentos de layout reutilizan cabecera, pie y scripts.')
p('home.css y home.js gestionan la presentación y navegación de las secciones. El ajuste visual de secciones y el desplazamiento del navegador no implican nuevas consultas de negocio por cada movimiento de scroll. Bootstrap aporta la rejilla y los componentes adaptables; las imágenes decorativas son recursos estáticos.')
block('Particularidad de Nosotros','“GET /nosotros devuelve la plantilla public/about. La composición visual y los textos de esta vista están definidos en HTML y CSS; no existe un CRUD de misión o visión. about.css aplica sus estilos específicos y el layout mantiene la navegación común”.')
block('Secuencia interna que se puede explicar','Navegador → filtros de seguridad → controlador → servicio → repositorio → Hibernate y JDBC → MySQL → Model → Thymeleaf → HTML. En una vista informativa como Nosotros se omite la consulta al repositorio porque el método del controlador no necesita datos del catálogo.')
block('Evidencia de código','PublicController.home y about; CatalogService.findCategories y findFeaturedProducts; ProductRepository.findTop3ByActiveTrueOrderByNameAsc; templates/public/index.html y about.html; static/css/home.css y about.css.')
block('Resultado observable','La página inicial muestra información obtenida de la base junto con contenido editorial estático. El mismo encabezado permite continuar el recorrido sin perder coherencia visual.')

page('3 Catálogo y detalle del producto','Tiempo orientativo 2 minutos')
block('Acción en pantalla','Abre /productos, selecciona una categoría, escribe un término, cambia el orden y abre la vista rápida. Finalmente entra al detalle de un producto.')
block('Diálogo técnico','“El filtro de categoría viaja al servidor como parámetro categoria. PublicController obtiene los productos activos de esa categoría y Thymeleaf genera el listado. La búsqueda y el orden del catálogo público se ejecutan después en el navegador sobre las tarjetas cargadas; escribir una letra no dispara una consulta SQL”.')
block('Lógica y dependencias','CatalogService.findActiveProducts elige una consulta de ProductRepository según exista categoryId. @EntityGraph carga la categoría junto con el producto para evitar lecturas adicionales por cada tarjeta. JPA trabaja con open-in-view desactivado, por lo que la información necesaria se prepara dentro del servicio.')
p('catalog.js normaliza los textos con Unicode NFD, elimina tildes para comparar y utiliza Intl.Collator para ordenar en español. Modifica la visibilidad de las tarjetas y conserva q y orden en la URL mediante History API. Se trata de JavaScript nativo; la categoría continúa siendo un filtro del servidor.')
block('Vista rápida y detalle','“La vista rápida utiliza un modal de Bootstrap. El detalle corresponde a GET /productos/{id}. CatalogService.findProduct solo devuelve un producto activo; si no existe o no está publicado, responde con 404. El servicio inicializa las fotografías adicionales dentro de la transacción de lectura”.')
p('La entidad Product calcula las etiquetas de precio y disponibilidad. PriceMode distingue CONSULT, FIXED y FROM; BigDecimal representa el importe. SupplyMode distingue MADE_TO_ORDER y STOCK. El precio mostrado no inicia una transacción de pago.')
block('Fotografías y carga','Los encuadres usan focusX y focusY y estilos de presentación. Las imágenes posteriores pueden cargarse de forma diferida mediante loading="lazy". Una imagen fuera de pantalla todavía no cargada no demuestra por sí sola que el archivo esté roto.')
block('Evidencia de código','PublicController.products y productDetail; CatalogService.findActiveProducts y findProduct; ProductRepository; Product.getPriceLabel y getAvailabilityLabel; public/products.html, product-modal.html y product-detail.html; static/js/catalog.js.')

page('4 Servicios y Galería','Tiempo orientativo 1 minuto y medio')
block('Acción en pantalla','Abre /servicios, muestra las seis propuestas y el acceso de consulta. En /galeria selecciona Flores y abre una fotografía para llegar al producto correspondiente.')
block('Diálogo para Servicios','“Servicios explica lo que ofrece la empresa y orienta al cliente hacia Contacto. Spring MVC atiende GET /servicios y devuelve public/services. Su contenido es editorial: no se consulta una tabla de servicios ni se administra desde el panel actual”.')
p('Thymeleaf incorpora los fragmentos comunes. Bootstrap, Bootstrap Icons y editorial-pages.css construyen las tarjetas, las columnas y la adaptación a pantallas pequeñas. El fondo decorativo se sirve como imagen estática. Los botones son enlaces de navegación a /contacto; no crean pedidos ni reservas.')
block('Diálogo para Galería','“La galería reutiliza el catálogo publicado. No mantenemos una segunda base de fotografías comerciales: cada elemento corresponde a un Product y conduce a /productos/{id}. El filtro categoria vuelve al servidor y la consulta selecciona los registros activos”.')
p('PublicController.gallery incorpora products, categories y selectedCategory al Model. CatalogService y los repositorios recuperan los datos con Spring Data JPA. public/gallery.html utiliza th:each para construir la cuadrícula y aplica los encuadres guardados. La colección Pequeños Grandes Detalles es un bloque editorial adicional de la plantilla.')
block('Qué conviene destacar','La integración se aprecia cuando se publica o archiva un producto desde el panel: las siguientes lecturas del catálogo y de la galería utilizan el mismo estado persistido. Las dos vistas presentan el mismo dominio con objetivos visuales diferentes, sin duplicar la lógica de publicación.')
block('Comprobación observable','Al seleccionar Flores debe cambiar la URL, el conjunto visible y el contador. Al abrir una tarjeta, su nombre y fotografía deben corresponder al detalle. El enlace Explorar el catálogo conserva la categoría seleccionada.')
block('Evidencia de código','PublicController.services y gallery; CatalogService.findActiveProducts; templates/public/services.html y gallery.html; static/css/editorial-pages.css; fragments/layout.html para Bootstrap, iconos y recursos comunes.')

page('5 Contacto y seguimiento de consultas','Tiempo orientativo 2 minutos')
block('Acción en pantalla','Abre /contacto y muestra redes oficiales, campos obligatorios y contador. Envía una consulta de prueba dentro del entorno demo. Más adelante localízala en /admin/inquiries y cambia su estado.')
block('Diálogo técnico','“El formulario enlaza sus campos con InquiryForm mediante th:object y th:field. Al enviar se realiza POST /contactos. @Valid aplica Bean Validation y BindingResult recoge los errores. La validación del navegador ayuda al usuario, pero la decisión de aceptar los datos se toma también en el servidor”.')
block('Reglas verificables','Nombre obligatorio de hasta 120 caracteres; correo obligatorio, con formato válido y hasta 180; teléfono opcional hasta 40; mensaje obligatorio hasta 2000. contact.js actualiza el contador y dirige el foco al error o a la confirmación. El contador no almacena la consulta ni sustituye las restricciones del DTO.')
block('Persistencia y respuesta','“Si hay errores, el controlador devuelve la misma plantilla y conserva los campos. Si los datos son válidos, InquiryService.create construye Inquiry y InquiryRepository lo guarda. Después se utiliza Post Redirect Get: el POST termina en una redirección y el siguiente GET muestra un mensaje flash”.')
p('Spring Security mantiene CSRF en el envío. Thymeleaf integra el token en los formularios procesados por Spring. @Transactional delimita el guardado y Hibernate persiste la entidad en MySQL. Este flujo registra una consulta: no envía automáticamente un correo y no existe una dependencia de correo declarada en pom.xml.')
block('Continuación en administración','AdminController.inquiries consulta los registros ordenados por fecha descendente. POST /admin/inquiries/{id}/status utiliza InquiryService.updateStatus para pasar entre NEW, READ y RESOLVED. Actualizar una entidad gestionada dentro de la transacción permite que Hibernate detecte y persista el cambio de estado.')
block('Redes y alcance','Instagram, Facebook, TikTok y Pinterest son enlaces a canales externos. No hay integración con sus APIs, sincronización de mensajes ni autenticación social. Una consulta tampoco descuenta stock ni constituye una compra.')
block('Evidencia de código','InquiryForm; PublicController.submitInquiry; InquiryService; InquiryRepository; AdminController.updateInquiryStatus; templates/public/contact.html; static/js/contact.js.')

page('6 Autenticación y panel administrativo','Tiempo orientativo 1 minuto y medio')
block('Acción en pantalla','Accede a /login con la cuenta del entorno demo. Muestra /admin, sus indicadores y los accesos para volver a la tienda. Evita proyectar contraseñas o archivos de configuración local.')
block('Diálogo técnico','“Spring Security intercepta la solicitud antes de llegar al controlador. El formulario de acceso utiliza UserDetailsService para buscar al usuario en AdminUserRepository. BCryptPasswordEncoder verifica la contraseña frente a su hash. La contraseña no se compara con texto plano almacenado en la tabla”.')
block('Sesión y autorización','SecurityConfig declara públicos el catálogo, las páginas informativas y los recursos. Las rutas /admin/** exigen ROLE_ADMIN. Un enlace oculto mejora la interfaz, pero la protección real es la cadena de filtros: escribir una ruta administrativa no evita la comprobación del rol. Tras autenticarse, el destino configurado es /admin.')
p('La autenticación se mantiene mediante sesión HTTP y cookie; no se implementa un esquema JWT. Las mutaciones conservan CSRF. El cierre de sesión invalida la sesión, por lo que también desaparece la preferencia de autollenado almacenada en ella.')
block('Indicadores del resumen','“AdminController.dashboard pide los conteos a CatalogService e InquiryService. Las tarjetas muestran productos, categorías, borradores, stock bajo y consultas. Son indicadores de esos registros; no representan ventas ni ingresos porque el sistema no incluye transacciones comerciales”.')
block('Navegación entre espacios','WorkspaceNavigationAdvice agrega el contexto del administrador a las vistas públicas. Thymeleaf muestra Ir al panel y, cuando corresponde, un acceso de gestión contextual. AdminViewAdvice y las plantillas administrativas preparan el retorno a la tienda. Ambas áreas comparten la sesión dentro de la misma instancia.')
block('Dependencias activas','starter-security para filtros, rol, sesión y BCrypt; starter-data-jpa y MySQL para recuperar AdminUser y los conteos; starter-thymeleaf para la vista de acceso y los fragmentos; Bootstrap y CSS propio para el panel.')
block('Evidencia de código','SecurityConfig.securityFilterChain, userDetailsService y passwordEncoder; AdminUser; AdminUserRepository; AdminController.dashboard; WorkspaceNavigationAdvice; AdminViewAdvice; auth/login.html y fragments/admin-layout.html.')

page('7 Categorías y gestión de productos','Tiempo orientativo 3 minutos')
block('Acción en pantalla','En /admin/categories muestra una categoría existente. En /admin/products/new crea Abrazo de Primavera como borrador, selecciona STOCK y precio fijo de ejemplo S/ 89.90. Completa descripción e imagen. Guarda, edita y publica el producto.')
block('Diálogo técnico','“El formulario se enlaza a ProductForm, que separa los datos recibidos por HTTP de la entidad persistida. ProductAdminController valida el DTO y comprueba la categoría. CatalogService aplica las reglas y utiliza ProductRepository para guardar en una transacción”.')
block('Campos y reglas','Nombre hasta 120 caracteres, descripción hasta 1000, categoría, modalidad, estado, precio, composición, personalización, ocasiones, preparación y notas internas. También se admiten indisponibilidad temporal, umbral de stock y encuadre. @AssertTrue exige descripción e imagen para publicar y un importe cuando el precio es FIXED o FROM. Los importes usan BigDecimal con dos decimales.')
p('El alta genera un SKU con prefijo DE y un fragmento de UUID. PublicationStatus distingue DRAFT, PUBLISHED y ARCHIVED, y Product sincroniza active con la publicación. El catálogo público consulta productos activos. Archivar conserva el registro y su historial; la ruta terminada en /delete ejecuta archivado, no borrado físico del producto.')
block('Edición y duplicación','CatalogService.updateProduct obtiene un bloqueo de escritura y compara la versión recibida con Product.version, anotada con @Version. Esto permite detectar formularios desactualizados. La duplicación crea otro producto como borrador y no copia existencias ni reservas. Cambiar de STOCK a MADE_TO_ORDER exige dejar ambas cantidades a cero.')
block('Categorías','CatalogService valida nombres repetidos sin distinguir mayúsculas y captura también conflictos de integridad. No permite eliminar una categoría con productos asociados. CategoryRepository y las relaciones JPA mantienen la conexión entre categoría y producto.')
block('Resultado y evidencia','Comprueba que el borrador no aparezca en el sitio y que, tras publicarlo, se muestre en catálogo y galería. Código: ProductAdminController; CatalogService.createProduct, updateProduct y applyForm; ProductForm; Product; CategoryForm; AdminController; admin/product-form.html.')

page('8 Fotografías y almacenamiento','Apoyo durante la creación del producto')
block('Acción en pantalla','Selecciona una fotografía JPG o PNG, revisa su previsualización y ajusta el punto de encuadre. Si necesitas mostrar la galería del producto, incorpora una imagen adicional y guarda el formulario.')
block('Diálogo técnico','“La subida de fotografías es una operación HTTP diferente del guardado del producto. MediaController recibe un MultipartFile en POST /admin/media y responde con JSON que contiene la URL. Después el formulario del producto utiliza esa ruta como dato de la imagen”.')
block('Procesamiento real del archivo','MediaStorageService usa ImageIO y BufferedImage del JDK para comprobar el formato y decodificar el contenido. No confía únicamente en la extensión del archivo. Acepta JPG y PNG de hasta 5 MB, con al menos 64 píxeles por lado y hasta 24 megapíxeles. Reduce el lado mayor a un máximo de 1600 píxeles sin ampliar imágenes pequeñas.')
p('La imagen se recodifica, se guarda con un UUID y se devuelve una ruta /media/{archivo}. Los límites generales multipart de Spring son 6 MB por archivo y 7 MB por solicitud; el servicio aplica el límite de negocio más estricto de 5 MB. Un fallo produce una respuesta de error en vez de aceptar silenciosamente el contenido.')
block('Dónde se guarda cada cosa','“MySQL conserva las rutas y los datos del producto. Los archivos subidos se guardan en el sistema de archivos, en uploads o uploads-demo según el entorno. Las fotografías iniciales del catálogo también pueden ser recursos estáticos incluidos en assets/products”.')
p('GET /media/{filename} utiliza PathResource. El servicio comprueba el patrón de nombre y que la ruta permanezca dentro del directorio permitido. MediaController define el tipo de contenido y una caché de una hora para la respuesta.')
block('Encuadre y galería','focusX y focusY son porcentajes entre 0 y 100: controlan la presentación mediante CSS, no recortan destructivamente el archivo original. ProductForm permite hasta cuatro rutas adicionales además de la principal. Product.getGalleryImages elimina duplicados y conserva el orden de presentación.')
block('Dependencias y evidencia','Spring MVC y MultipartFile para transporte; ImageIO, AWT y NIO del JDK para procesamiento y disco; Jackson integrado en el soporte web para JSON; JPA para rutas y colección de imágenes. Revisar MediaController, MediaStorageService, ProductForm y Product.')

page('9 Inventario y consistencia de saldos','Tiempo orientativo 2 minutos')
block('Acción en pantalla','Abre /admin/inventory y el detalle del nuevo producto. Registra los movimientos del ejemplo. En cada operación indica el motivo y muestra el saldo y el historial actualizado.')
block('Diálogo técnico','“El stock físico y las reservas se conservan por separado. La disponibilidad se calcula como físico menos reservado. InventoryController recibe StockAdjustmentForm y delega en InventoryService.adjust; la regla aritmética está concentrada en el record StockBalance”.')
table(['Movimiento','Físico','Reservado','Disponible'],[('Inicio','0','0','0'),('Entrada IN de 10','10','0','10'),('Reserva RESERVE de 2','10','2','8'),('Liberación RELEASE de 1','10','1','9'),('Salida OUT de 3','7','1','6')],[3,1.2,1.2,1.2])
block('Interpretación correcta','IN suma unidades físicas; OUT las resta sin liberar reservas implícitamente; RESERVE aumenta reservado; RELEASE lo disminuye. CORRECTION fija un saldo físico absoluto, no suma una diferencia. Si se pretende entregar unidades reservadas, hay que liberar la reserva correspondiente antes de registrar la salida según las reglas actuales.')
block('Garantías de negocio y concurrencia','StockBalance impide cantidades negativas y reservas superiores al físico; aplica un límite de un millón de unidades. InventoryService rechaza movimientos en productos por encargo, versiones desactualizadas, motivos vacíos y ajustes que no cambian el saldo. Una salida que invada unidades reservadas se rechaza porque produciría un saldo inválido.')
p('ProductRepository.findForUpdate utiliza @Lock(PESSIMISTIC_WRITE). @Transactional mantiene la lectura, modificación y creación de StockMovement en una unidad de trabajo. El bloqueo serializa escrituras sobre el producto y la comparación de versión detecta una pantalla antigua. @Version añade control de concurrencia de JPA.')
block('Trazabilidad','StockMovement registra tipo, cantidad, saldos anteriores, motivo, responsable y fecha. El historial se consulta en páginas de 20 registros. Un nivel disponible menor o igual al umbral marca stock bajo. Estos movimientos son administrativos; no provienen de un checkout o pasarela de pago.')
block('Evidencia de código','InventoryController; InventoryService.adjust e history; StockBalance.apply; StockAdjustmentForm; ProductRepository.findForUpdate; Product.getAvailableQuantity; StockMovement y StockMovementRepository.')

page('10 Entorno demo y autollenado','Tiempo orientativo 1 minuto y medio')
block('Diálogo técnico','“El entorno demo y el autollenado son dos mecanismos diferentes. El perfil demo utiliza una base MySQL independiente y datos persistentes. El autollenado únicamente completa campos del formulario y no guarda ni publica hasta que confirmamos la acción normal”.')
table(['Aspecto','Entorno principal','Entorno demo'],[('Puerto predeterminado','8080','8081'),('Base MySQL','deluxe_eterno','deluxe_eterno_demo'),('Cookie de sesión','JSESSIONID','DELUXE_DEMO_SESSION'),('Archivos subidos','uploads','uploads-demo'),('Configuración','application.yml','application-demo.yml y variables locales')],[1.7,2.45,2.45])
block('Precarga y protección','@Profile("demo") activa DemoDatabaseGuard, DemoDataInitializer y DemoDatasetService. Antes de que Hibernate modifique el esquema, el guard verifica el nombre de la base y el marcador demo_environment con app_key deluxe-eterno-demo. No basta con renombrar la conexión: la identidad debe coincidir.')
p('DemoDatasetService.initialize consulta dataset_version y evita repetir la precarga cuando la versión ya coincide. Incluye productos con estados y disponibilidades variados, precios de ejemplo, movimientos y consultas. reset verifica de nuevo la identidad y ejecuta borrado y precarga mediante DML dentro de una transacción. El restablecimiento es explícito; no ocurre en cada visita.')
block('Autollenado por sesión','DemoController.toggle modifica un atributo de HttpSession. DemoViewAdvice determina su disponibilidad y los fragmentos muestran los controles. demo.js asigna ejemplos a los campos y dispara eventos de entrada y cambio. No constituye una base de datos del navegador ni evita @Valid, CSRF o la autorización administrativa.')
block('Operación para la exposición','El script scripts/demo.ps1 administra el entorno. Comprueba previamente su arranque y los datos cargados. No restablezcas la base durante el recorrido: las altas, cambios y consultas de la demostración se guardan realmente en la base demo y deben permanecer hasta completar la comprobación integrada.')
block('Punto de sustentación','H2 se utiliza en pruebas automatizadas; deluxe_eterno_demo es MySQL. El autollenado puede estar disponible en otra instancia si la configuración lo permite: activar ese botón por sí solo no cambia de base de datos.')

page('11 Verificación integrada y cierre','Tiempo orientativo 2 minutos')
block('Comprobación final en pantalla','Vuelve a /productos desde el panel, busca Abrazo de Primavera y abre su detalle. Comprueba nombre, precio, fotografía y disponibilidad. Regresa a /admin/inquiries, identifica la consulta enviada y actualiza su estado. Muestra que el historial de inventario conserva los movimientos y el responsable.')
block('Diálogo de cierre','“La demostración recorre un flujo completo: los datos que administra el usuario autorizado se reflejan en el sitio público y las consultas del sitio llegan al panel. Las reglas residen en los servicios y los DTO, y no dependen solamente de los botones o de JavaScript. La base demo permite reproducirlo con datos separados”.')
block('Cómo se comprueba con pruebas','La última ejecución registrada en docs/modulo-productos/pruebas-editoriales.log terminó con BUILD SUCCESS: 59 pruebas, cero fallos y cero errores. Este resultado corresponde a esa ejecución del proyecto; no es una garantía de ausencia absoluta de defectos ni sustituye la revisión visual.')
p('Maven ejecuta las pruebas con las dependencias de test. MockMvc simula solicitudes contra Spring MVC; spring-security-test permite evaluar usuarios, roles y CSRF. H2 proporciona persistencia de prueba. Los escenarios de aislamiento verifican reglas del entorno demo, pero no convierten H2 en un sustituto idéntico de MySQL para todas las características del motor.')
table(['Área','Clases de prueba de referencia'],[('Arranque y regresiones','DeluxeEternoApplicationTests y AuditRegressionTests'),('Catálogo y gestión','ProductCatalogIntegrationTests y ProductManagementTests'),('Flujo y navegación','ExhibitionIntegrationTests y WorkspaceNavigationTests'),('Autollenado y aislamiento','DemoModeTests, DisabledDemoTests, IsolatedDemoTests y DemoDatabaseGuardTests')],[2,4.6])
block('Preguntas técnicas frecuentes','¿Es una API REST completa? No: predomina Spring MVC con HTML renderizado; la subida de imágenes sí devuelve JSON. ¿Hay compra en línea? El alcance actual es catálogo, consultas y administración, sin carrito, pago o pedido. ¿Hay microservicios? No: es una aplicación monolítica organizada por capas. ¿Toda búsqueda consulta MySQL? No: la búsqueda pública trabaja sobre las tarjetas cargadas; el filtro público de categoría sí consulta al servidor.')
block('Dónde localizar la explicación','Dependencias: pom.xml. Configuración: src/main/resources/application.yml y application-demo.yml. Rutas: web y web/admin. Reglas: service y dto. Persistencia: domain y repository. Presentación: templates y static. Aislamiento: demo. Pruebas: src/test/java/com/deluxeterno. Estas referencias corresponden al código del proyecto revisado para preparar el guion.')

OUT.parent.mkdir(parents=True,exist_ok=True)
d.save(OUT)
print(OUT)
