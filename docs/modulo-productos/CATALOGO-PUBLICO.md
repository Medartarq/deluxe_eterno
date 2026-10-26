# Catálogo público rediseñado

Se actualiza /productos con cabecera de marca, fondo floral existente, acceso a contacto, filtros de categorías más claros, búsqueda inmediata, orden alfabético y contador de resultados. Las tarjetas separan categoría, nombre, descripción, precio y disponibilidad; la fotografía y el título enlazan a la ficha, y la vista rápida permanece disponible desde la imagen.

La categoría se filtra en el servidor como antes. La búsqueda y la ordenación se aplican en el navegador sobre los productos publicados de esa categoría. La búsqueda ignora tildes y diferencias de mayúsculas y combina palabras. Los parámetros q y orden se conservan en la URL y al cambiar de categoría. Sin JavaScript, el catálogo y los enlaces de categorías/fichas siguen disponibles y los controles de búsqueda se ocultan.

La revisión de la captura detectó carga diferida, no errores: al abrir el catálogo original había seis fotografías cargadas y 29 pendientes de acercarse a la pantalla, sin imágenes rotas detectadas. Se conservan los encuadres específicos e inline configurados anteriormente. Las primeras seis fotos se solicitan de inmediato y el resto conserva carga diferida. Se añade un mensaje alternativo ante un error real de imagen.

Los estilos catalog.css solo se cargan en Productos. La cuadrícula pasa de tres a dos y una columna según el ancho. La página usa desplazamiento normal, sin el ajuste entre secciones del inicio. No se modifican datos, precios, estados de publicación ni esquema de base de datos.

Verificación: 59 pruebas existentes aprobadas y sintaxis de catalog.js comprobada con Node. Revisión visual en localhost:8080: búsqueda rosas (seis coincidencias del catálogo completo), cambio a Flores conservando búsqueda (dos de cinco), orden Z–A, estado sin coincidencias, limpiar búsqueda y vista rápida de Susurro de Lirios. Las fotografías y los controles responden correctamente. Las reglas móviles se revisan en código; no se utiliza un dispositivo móvil físico en esta sesión.

Captura local: catalogo-redisenado.png, ignorada por Git.
