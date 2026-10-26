# Módulo de productos e inventario

Implementado el 26 de octubre de 2026 para ejecución académica local.

## Acceso y uso

Iniciar sesión con la cuenta administradora existente en `/login`. En el panel aparecen Productos e Inventario. Todas las escrituras requieren rol ADMIN y token CSRF. El rol STAFF conserva sus permisos anteriores: no accede a este módulo. No se crean cuentas ni contraseñas nuevas en la instalación real.

Productos permite buscar por nombre/código y filtrar por categoría, publicación y modalidad. Crear, editar, duplicar como borrador, publicar y archivar son operaciones separadas. Archivar conserva fotografías e historial; no existe eliminación definitiva desde este panel.

El editor incluye nombre, categoría, descripción, ocasiones, contenido del regalo, personalización, preparación, precio a consultar/fijo/desde en soles, disponibilidad temporal y notas internas. El código se genera automáticamente. Publicar exige nombre, categoría, descripción y fotografía principal. Los borradores pueden carecer de descripción/fotografía. Las notas internas no se muestran en la tienda.

Se admiten hasta cinco fotografías, una principal y cuatro adicionales. Se pueden subir JPG/PNG o reutilizar fotografías del catálogo. Hacer principal, anterior y quitar permiten organizar la selección. El encuadre horizontal/vertical modifica la presentación de la tarjeta sin modificar el archivo fuente. La ficha pública presenta la galería completa. Guardar confirma la selección; subir un archivo por sí solo todavía no lo vincula a un producto.

## Stock y reservas

Los productos existentes mantienen su publicación previa y se interpretan como por encargo y precio a consultar; no se inventan precios ni existencias. El modo por unidades empieza en cero. El disponible es físico menos reservado, nunca negativo.

- Entrada: suma al físico.
- Salida: descuenta unidades libres del físico.
- Reserva: compromete unidades disponibles.
- Liberación: reduce las unidades reservadas.
- Corrección: establece el total físico contado, no una diferencia. Admite cero cuando no hay reservas.

Para entregar una reserva, registrar su liberación y después la salida, con motivos que relacionen ambos movimientos. Son dos operaciones manuales; no hay pedidos ni entregas automáticas en esta versión. Enviar una consulta de cliente no reserva ni descuenta unidades.

Cada movimiento conserva fecha UTC, usuario autenticado, motivo, cantidad y ambos saldos anteriores/posteriores. Se guarda junto con el saldo dentro de una transacción y se bloquea el producto durante el ajuste. Los formularios nuevos incluyen versión para rechazar ediciones desactualizadas. No es posible cambiar a por encargo manteniendo unidades o reservas. La alerta de stock bajo usa el disponible y el umbral configurado.

## Persistencia y colaboración

`application.yml` conserva `ddl-auto: update`: al reiniciar, Hibernate agrega columnas a `products` y crea `product_images` y `stock_movements`. Las columnas de conteo, reservas y versión tienen valor inicial cero; encuadre 50 y umbral 3. Los enums nuevos pueden ser nulos en registros antiguos: sus getters mantienen valores compatibles. No hay borrado ni recreación de tablas.

Antes del primer reinicio con datos reales, realizar una copia de seguridad habitual de MySQL. La actualización sobre una copia de la base MySQL existente no se ha ejecutado en esta sesión: las verificaciones utilizan H2. Si aparecen errores DDL, detener el arranque y revisar los permisos/versión de MySQL; no cambiar a `create` o `create-drop`. Para volver a una versión anterior, restaurar conjuntamente base y archivos desde la copia.

Las nuevas imágenes se guardan fuera del classpath en `./uploads`, configurable con `MEDIA_DIRECTORY`. El proceso necesita permisos de escritura allí. Se valida el contenido decodificado, tamaño máximo 5 MB, mínimo 64 píxeles por lado y máximo 24 megapíxeles. Se reencodan sin metadatos y se limitan a 1600 píxeles en el lado mayor. No se conserva una segunda copia del original subido; los originales ya existentes en Productos y assets no se alteran. No se admite SVG/WebP en la carga. Las rutas públicas usan nombres UUID y no permiten recorrer directorios.

Git ignora uploads: compartir código por GitHub no comparte fotos nuevas ni registros MySQL. Cada colaborador configura su base y administrador mediante las variables documentadas del proyecto. Para compartir contenido actualizado, intercambiar por un canal acordado un respaldo de la base y la carpeta de imágenes, sin publicar credenciales. Respaldar ambos juntos. Las fotos que se suben y luego se descartan permanecen en almacenamiento; no hay depuración automática para evitar eliminar imágenes compartidas por productos duplicados. Las URLs de fotos son públicas incluso si el producto está archivado: no subir documentos privados.

## Verificación

Se ejecuta la suite completa de Maven, incluidas regresiones del catálogo y seguridad y pruebas del nuevo módulo: vistas, borradores, galería, privacidad de notas, duplicado sin existencias, control de versión, restricciones de saldos y validación de cargas. La comprobación visual usa H2 aislado en 8081, con creación de borrador y registro de entrada verificando el nuevo saldo. La instancia del usuario en 8080 no se reinicia ni recibe datos de prueba. Evidencias locales (ignoradas por Git) en esta carpeta.

## Límites de esta entrega

No incluye variantes con existencias propias, inventario de insumos, precios por variante, órdenes de compra, pedidos ni reservas automáticas, roles nuevos o administración de usuarios. Las búsquedas administrativas cargan el catálogo actual completo, adecuado para el tamaño académico; para volúmenes mayores se deberá paginar y filtrar desde la base. Requiere JavaScript para la gestión visual de fotos y conexión a las CDN ya utilizadas por el proyecto para Bootstrap e iconos.

Resultado final de Maven: **44 pruebas, 0 fallos, 0 errores**. También se verificó visualmente la edición y publicación con precio desde, manteniendo las ocho unidades registradas en el entorno aislado.
