# Identidad visual del panel administrativo

26 de octubre de 2026.

Se incorpora la identidad de Deluxe Eterno mediante los recursos de marca existentes, superficies blancas, acentos coral/rosa, texto carbón y acciones principales burdeos. Se mantienen los colores semánticos de estados. Los estilos nuevos se encuentran en `src/main/resources/static/css/admin.css`, cargado únicamente por las plantillas administrativas.

## Cambios

- Navegación lateral con logotipo, iconos, sección activa (`aria-current`), usuario autenticado y salida mediante POST con CSRF.
- En anchos menores de 992 px, navegación desplegable mediante botón Menú. Las tablas de productos, inventario y consultas se presentan como tarjetas con etiquetas a partir de ese ancho; en móvil se apilan indicadores y filtros.
- Resumen con accesos a consultas nuevas, stock bajo/agotado y borradores. Los dos nuevos conteos se consultan directamente en base de datos sin cargar productos completos. Se conserva el resumen de productos, categorías y consultas totales.
- Tabla de productos más compacta: Editar permanece visible y Más despliega stock, duplicación y archivo/publicación. El despliegue nativo permite acceso con teclado y evita recortes de menús flotantes en tablas.
- Navegación por anclas del editor, acceso directo a vista previa y barra de guardado adhesiva.
- Tarjetas de categorías con acción destructiva separada visualmente; consultas con estado vacío explicativo y etiquetas de estado en español.
- Pie administrativo ligero. Corrección de estilos generales que imponían espaciados de la tienda al resumen.

## Verificación y alcance

`mvn test`: 44 pruebas, cero fallos y cero errores. La prueba existente de inventario comprueba también que un borrador con tres unidades disponibles y umbral tres se cuenta correctamente en ambos indicadores del resumen. Se mantiene la regresión que exige no cargar entidades completas en el dashboard.

Se revisaron visualmente resumen, catálogo, menú Más, editor con vista previa y guardado, y estado vacío de consultas en una instancia H2 aislada en el puerto 8081. Captura local: `panel-identidad.png` (ignorada por Git). Se revisaron las reglas adaptables; no se realizó una prueba visual en un dispositivo móvil físico en esta sesión.

Este rediseño no requiere migraciones ni modifica registros MySQL. Para verlo completo en la instancia del usuario, reiniciar Spring Boot con su configuración habitual y recargar la página. Las funciones y protecciones previas del módulo siguen vigentes. No se incorporaron dependencias nuevas.
