# Deluxe Eterno — Spring Boot

Aplicación web full-stack para el proyecto académico Deluxe Eterno. Esta carpeta contiene la evolución del prototipo estático hacia una aplicación renderizada en servidor.

## Stack

- Java 26
- Spring Boot 4.1.1
- Maven
- Spring Web MVC
- Thymeleaf
- Spring Data JPA/Hibernate
- MySQL
- Spring Security con autenticación por sesión y BCrypt
- Bootstrap 5 y Bootstrap Icons mediante CDN

## Estructura

```text
src/main/java/com/deluxeterno/
├── config/       Seguridad e inicialización
├── domain/       Entidades JPA y enums
├── dto/          Formularios validados
├── repository/   Repositorios Spring Data
├── service/      Lógica de catálogo y consultas
└── web/          Controladores públicos y administrativos

src/main/resources/
├── templates/    Vistas Thymeleaf
├── static/       CSS, JavaScript y assets
└── application.yml
```

## Requisitos

- JDK 26
- Maven 3.6.3 o superior
- MySQL 8 o superior para ejecutar la aplicación

El proyecto de pruebas usa H2 en memoria mediante el perfil `test`; no requiere MySQL para ejecutar `mvn test`.

## Configuración local

Crear la base de datos MySQL `deluxe_eterno` y configurar las variables antes de iniciar:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/deluxe_eterno?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="tu_password"
$env:ADMIN_USERNAME="admin"
$env:ADMIN_PASSWORD="una_password_local"
mvn spring-boot:run
```

La contraseña administrativa no se guarda en el repositorio. El usuario inicial solo se crea si `ADMIN_PASSWORD` está definida y la tabla de usuarios está vacía.

## Comandos

Para colaborar mediante GitHub, consultar [CONTRIBUTING.md](CONTRIBUTING.md). El workflow de verificación usa H2 para pruebas y no necesita credenciales de MySQL.

```powershell
mvn test
mvn clean verify
mvn spring-boot:run
```

La aplicación queda disponible en `http://localhost:8080`.

## Rutas iniciales

### Público

- `/`
- `/nosotros`
- `/productos`
- `/productos/{id}`
- `/servicios`
- `/galeria`
- `/contacto`
- `POST /contactos`

### Panel protegido

- `/login`
- `/admin`
- `/admin/products`
- `/admin/categories`
- `/admin/inquiries`

## Alcance actual

- Catálogo dinámico de categorías y productos.
- Detalle de producto.
- Consultas públicas validadas y persistidas.
- Panel administrativo protegido para productos, categorías y estados de consulta.
- Catálogo inicial de 35 productos con fotografías propias de la entrega, siete categorías, una segunda vista de Ruta de Aventuras y una colección de minirramos en la galería.
- Fotografías completas en tarjetas verticales, detalle y vista rápida, con carga diferida.

## Importación del catálogo

Al iniciar Spring Boot, `CatalogDataInitializer` carga los productos de `src/main/resources/catalog/products.properties`. Cada producto importado tiene una clave estable: los siguientes arranques no duplican registros ni sobrescriben ediciones o desactivaciones del administrador. Las categorías públicas solo incluyen productos activos. Las categorías anteriores permanecen disponibles en administración.

El inventario editorial original se conserva en `Productos/catalogo-productos.json`. Para volver a exportar los recursos desde ese inventario puede ejecutarse `docs/integrar_productos.py` con Python; solo copia fotografías y genera el archivo de propiedades. Los JPG originales permanecen intactos. Las tarjetas usan un marco 4:5 con recorte visual moderado y posiciones específicas para las fotografías revisadas. El borde negro de Jardín de Caricias se oculta en su miniatura; Spider-Man conserva la imagen completa para no añadir cortes. El detalle y el modal muestran las fotografías originales completas.

Después de actualizar el código hay que reiniciar la instancia con su configuración habitual de MySQL. Hibernate, con `ddl-auto=update`, agrega las columnas `catalog_key` y `secondary_image_path`. Si se usa `validate`, aplicar previamente la migración equivalente. La importación se ejecuta dentro de una transacción.

Las siguientes etapas del curso pueden agregar servicios, galería, canales de contacto administrables, API REST y JWT, sin modificar el prototipo APF1 preservado en `../deluxe_eterno_avance/`.

## Gestión de productos e inventario

El panel ADMIN incluye editor visual, fotografías, borradores/publicación, precios y stock con movimientos auditables. Consulta [la guía del módulo](docs/modulo-productos/README.md) para uso, actualización de esquema y almacenamiento de imágenes mediante `MEDIA_DIRECTORY`. Las fotos nuevas y los registros MySQL se respaldan y comparten por separado de GitHub.

## Exposición y modo demo

Tras reiniciar, un administrador puede pulsar **Activar demo** en el panel. El modo habilita autollenado de categorías, productos, stock y contacto en su sesión, sin guardar automáticamente ni cambiar permisos. La pantalla `/admin/demo` contiene el recorrido recomendado. Los datos guardados persisten en la base actual. Puede deshabilitarse la función con `DEMO_AVAILABLE=false`. Consulta [el guion y la verificación de integración](docs/modulo-productos/DEMO-E-INTEGRACION.md).
