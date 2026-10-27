# Creación de usuarios

## Uso

1. Inicia sesión como administrador.
2. Abre **Usuarios** en el menú del panel. En pantallas pequeñas, despliega **Menú**.
3. Selecciona **Nuevo usuario**.
4. Escribe un nombre único, la contraseña y su confirmación.
5. Pulsa **Crear usuario**. La nueva cuenta aparecerá en el listado y podrá iniciar sesión.

Todas las cuentas creadas por este módulo tienen permisos de administrador, incluida la creación de otras cuentas. El rol STAFF existente no tiene acceso al panel; por ese motivo no se ofrece como opción de alta.

El nombre se guarda en minúsculas, admite de 3 a 80 caracteres y puede contener letras sin tildes, números, puntos, guiones y guiones bajos. La contraseña requiere al menos 12 caracteres y un máximo de 72 bytes UTF-8. Ambas contraseñas deben coincidir. Ante un error se conserva el nombre, pero es necesario volver a introducir las contraseñas.

## Rutas y componentes

- `GET /admin/users`: listado de cuentas sin hashes de contraseña.
- `GET /admin/users/new`: formulario de creación.
- `POST /admin/users`: validación, codificación y guardado.
- `UserForm`: valida nombre, contraseña, confirmación y límite de bytes.
- `UserService`: comprueba duplicados sin distinguir mayúsculas, utiliza BCrypt y guarda en una transacción. Convierte también los conflictos de unicidad en un error de formulario.
- `UserAdminController`: coordina las vistas y la redirección posterior al guardado.
- `AdminUserRepository`: añade consulta ordenada y comprobación de duplicados.

`SecurityConfig` ya protege todas las rutas `/admin/**` mediante ADMIN. Se mantiene CSRF; no hay registro público, selección de rol desde el cliente ni modificación de cuentas existentes. Se utiliza la tabla `admin_users` actual sin nuevas columnas. El administrador inicial de una instalación vacía se sigue creando mediante `ADMIN_USERNAME` y `ADMIN_PASSWORD`.

## Verificación

Suite Maven: **66 pruebas, cero fallos y cero errores**. Los siete nuevos escenarios comprueban vistas sin exposición de hashes, alta y autenticación real con Spring Security, duplicados, datos inválidos, límite UTF-8 de BCrypt, rechazo de visitantes/STAFF y protección CSRF/atributos no editables. La revisión visual se realizó en una instancia temporal local con H2 en el puerto 8082; no se añadieron cuentas de prueba a MySQL principal.

Después de actualizar el proyecto, reinicia la instancia principal con su configuración habitual. No basta con recargar las plantillas: las nuevas rutas y servicios requieren cargar las clases Java actualizadas.
