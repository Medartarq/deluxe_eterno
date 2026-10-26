# Colaboración local

## Entorno

Usar JDK 26 o compatible con bytecode 26, Maven 3.6.3 o superior y MySQL 8 o superior para ejecutar la aplicación. Las pruebas usan H2 y no requieren MySQL ni credenciales compartidas. No subir `.env`, contraseñas, volcados de datos personales ni configuraciones privadas del IDE.

1. Clonar el repositorio y crear una rama de trabajo.
2. Verificar `java -version` y `mvn -version`. El JDK seleccionado por Maven debe poder compilar para Java 26.
3. Ejecutar `mvn --batch-mode --no-transfer-progress clean verify`.
4. Crear una base local `deluxe_eterno` y definir `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` en la terminal. Consultar los ejemplos del README.
5. Para el primer administrador, definir `ADMIN_USERNAME` y `ADMIN_PASSWORD` localmente antes de arrancar. Solo se crea cuando la tabla de usuarios está vacía. Cambiar estas variables después no rota la contraseña almacenada.
6. Iniciar con `mvn spring-boot:run`. No usar el perfil `test` contra una base persistente: configura H2 temporal y `create-drop`.

## Pull requests

Mantener cambios pequeños, explicar el problema resuelto y adjuntar resultados de pruebas. Si cambia la presentación, incluir capturas del mismo tamaño antes y después. Mantener los originales de `Productos/` y verificar el inventario antes de modificar fotografías. No publicar capturas con consultas de personas reales.

El workflow `.github/workflows/verify.yml` ejecuta pruebas y empaquetado con Java 26 en push y pull request. Está preparado en el repositorio; su primera ejecución remota requiere subir los cambios y que GitHub Actions esté habilitado. No publica ni despliega la aplicación y solo solicita lectura del repositorio.

## Recursos gráficos

Los recursos finales ya están en `src/main/resources/static/assets/`: no hace falta ejecutar los generadores para compilar ni iniciar el sitio. Para mantenerlos:

- `docs/integrar_productos.py` exporta el inventario JSON y copia fotografías sin alterar sus píxeles; usa Python 3.9+ de biblioteca estándar.
- `docs/clasificar_productos.py` verifica el inventario y puede regenerar el catálogo editorial local sin renombrar nuevamente las fotos clasificadas.
- `docs/inspeccionar_productos.py` necesita Pillow y se detiene si ya existe el inventario; evita destruir la referencia histórica.
- `docs/generar_favicon.cjs` necesita Node y Sharp. Resuelve `sharp` desde el entorno del colaborador o desde la variable `SHARP_MODULE_PATH`, sin depender de una ruta personal fija. Las exportaciones existentes incluyen ICO; el generador JS regenera las versiones PNG.

## Documentación de auditoría

El informe y la matriz de cobertura están en `docs/auditoria/`. El respaldo local, registros de ejecución y capturas no se incluyen automáticamente en Git. Antes de crear un commit, revisar `git diff` y `git status`; no añadir todo el directorio de evidencia indiscriminadamente.
