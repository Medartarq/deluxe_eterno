# Deluxe Eterno — Spring Boot

Aplicación web full-stack para el proyecto académico Deluxe Eterno. Esta carpeta contiene la evolución del prototipo estático hacia una aplicación renderizada en servidor.

## Stack

- Java 26
- Spring Boot 4.1.1
- Maven
- Spring Web MVC
- Thymeleaf
- Spring Data JPA/Hibernate
- PostgreSQL
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
├── application.yml.example   Configuración base sin secretos
├── application-dev.yml       Valores de desarrollo
└── application-prod.yml      Valores seguros para producción
```

## Requisitos

- JDK 26
- Maven 3.6.3 o superior
- PostgreSQL 16 o superior para ejecutar la aplicación

El proyecto de pruebas usa H2 en memoria mediante el perfil `test`; no requiere MySQL para ejecutar `mvn test`.

## Configuración local

Crear la base de datos PostgreSQL `deluxe_eterno`. Luego copiar `src/main/resources/application.yml.example` como `application.yml` (este archivo está ignorado por Git) y configurar las variables antes de iniciar:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/deluxe_eterno"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="tu_password"
$env:ADMIN_USERNAME="admin"
$env:ADMIN_PASSWORD="una_password_local"
mvn spring-boot:run
```

La contraseña administrativa no se guarda en el repositorio. El usuario inicial solo se crea si `ADMIN_PASSWORD` está definida y la tabla de usuarios está vacía.

El perfil predeterminado es `dev`: Hibernate puede actualizar el esquema y Thymeleaf no usa caché. Para producción, establecer `SPRING_PROFILES_ACTIVE=prod`; el esquema se valida (`ddl-auto=validate`) y las plantillas usan caché. Las migraciones de base de datos deben aplicarse antes del despliegue.

### Migrar desde MySQL

La utilidad `scripts/migrate_mysql_to_postgresql.py` crea una base PostgreSQL nueva y copia las tablas de Deluxe Eterno sin modificar MySQL. Requiere Python 3.12 o superior y los conectores:

```powershell
python -m pip install mysql-connector-python "psycopg[binary]"
$env:MYSQL_PASSWORD="contraseña_mysql"
$env:POSTGRES_PASSWORD="contraseña_postgresql"
python scripts/migrate_mysql_to_postgresql.py
```

El script se detiene si la base PostgreSQL destino ya existe, para evitar sobrescribir información. Antes de migrar, crea un respaldo de MySQL y detén la aplicación para preservar la consistencia.

Para una base vacía de Render, usar la **External Database URL** solo de forma temporal en la terminal local:

```powershell
$env:MYSQL_PASSWORD="contraseña_mysql"
$env:POSTGRES_URL="postgresql://usuario:contraseña@host/base?sslmode=require"
python scripts/migrate_mysql_to_postgresql.py
```

No guardes esa URL en archivos versionados.

## Despliegue en Render

El `Dockerfile` compila la aplicación con Java 26 y el perfil `prod` aplica la migración Flyway `V1` en una base PostgreSQL vacía. En Render, crea un **Web Service** en la misma región que PostgreSQL, selecciona el entorno Docker y configura `SPRING_PROFILES_ACTIVE=prod`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `ADMIN_USERNAME` y `ADMIN_PASSWORD`. La URL JDBC debe usar el formato `jdbc:postgresql://host:5432/base`.

Render Free no conserva `uploads/`; las fotografías cargadas desde el panel deben almacenarse fuera de la instancia o se perderán al reiniciar o suspender el servicio.

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

## Creación de usuarios

Desde **Usuarios → Nuevo usuario** (`/admin/users/new`), un administrador puede crear otras cuentas de administración. El nombre es único sin distinguir mayúsculas y se guarda en minúsculas; admite entre 3 y 80 caracteres alfanuméricos, puntos, guiones y guiones bajos. La contraseña se confirma, exige al menos 12 caracteres y respeta el límite de 72 bytes de BCrypt. Se almacena únicamente su hash y no se vuelve a mostrar en los formularios.

Las nuevas cuentas tienen rol ADMIN, el único que actualmente accede al panel, y pueden gestionar catálogo, inventario, consultas y usuarios. No hay registro público. El módulo utiliza la tabla existente `admin_users`; no requiere una migración adicional. Consulta [la guía de usuarios](docs/modulo-productos/USUARIOS.md). Reinicia Spring Boot después de actualizar el código para cargar las nuevas rutas.

## Autollenado y entorno demo independiente

El autollenado completa formularios de la sesión sin guardarlos automáticamente. No selecciona la base de datos.

Para trabajar con datos separados y escenarios precargados, usa el perfil `demo` con MySQL `deluxe_eterno_demo`:

```powershell
.\scripts\demo.ps1 -Action Initialize
.\scripts\demo.ps1 -Action Start
```

La demo se abre en http://localhost:8081 y utiliza su propia cuenta MySQL, administrador, cookie y carpeta de imágenes. Initialize solicita las credenciales MySQL locales; las credenciales generadas de la demo se guardan en `.env.demo.local`, ignorado por Git. Reiniciar conserva cambios; `-Action Reset`, con la demo detenida, recupera los escenarios iniciales. Consulta [la guía del entorno demo](docs/modulo-productos/ENTORNO-DEMO.md).
