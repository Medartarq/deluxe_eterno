# Modo demo e integración para la exposición

Fecha: 26 de octubre de 2026.

## Activación

1. Reiniciar Spring Boot con la configuración habitual de MySQL para cargar los cambios.
2. Acceder con la cuenta administradora real a `/admin`.
3. Pulsar **Activar demo** en el menú lateral. Se abre `/admin/demo`, con el guion de exposición.
4. En categorías, productos, movimientos de stock y Contacto aparecerá **Completar campos**.

El modo comienza apagado en cada sesión. Solo ADMIN puede activarlo; el visitante normal no ve autollenado. Para usarlo en Contacto, mantener la misma sesión del navegador con la que se activó. Desactivar demo o cerrar sesión lo apaga. No se autocompletan contraseñas ni se crean usuarios especiales.

`DEMO_AVAILABLE=false` permite deshabilitar la función por configuración (requiere reiniciar). Por defecto está disponible para el uso académico local, pero no se activa sola.

## Comportamiento de los ejemplos

- **Categoría:** nombre con prefijo DEMO y sufijo aleatorio para repetir la exposición sin colisiones habituales, y descripción ficticia.
- **Producto por encargo:** nombre, categoría, descripción, contenido, personalización, ocasiones y preparación; precio a consultar y borrador.
- **Producto con stock:** lo anterior, modalidad por unidades, precio ilustrativo S/ 89.90 y umbral tres. El saldo continúa en cero hasta registrar una entrada.
- Ambos ejemplos reutilizan una fotografía local existente cuando el producto todavía no tiene imágenes. La galería existente no se reemplaza. La vista previa se actualiza usando el editor habitual.
- **Stock:** entrada de hasta diez unidades; reserva, liberación o salida de hasta dos, ajustadas al saldo. Las opciones incompatibles con el saldo se deshabilitan. Corrección propone un nuevo total físico válido. Todo sigue sometido a las validaciones del servidor y control de versión.
- **Contacto:** nombre y mensaje identificados como DEMO, correo ficticio `exposicion@example.test` y teléfono vacío. No se envían correos o mensajes externos.

Pulsar Completar campos **no guarda ni envía**. Si hay campos ya completados se solicita confirmación antes de reemplazarlos; el umbral inicial tres no dispara esta confirmación. Revisar el formulario y usar Crear, Guardar, Registrar movimiento o Enviar consulta como en el uso normal.

## Guion sugerido (8–10 minutos)

| Paso | Acción | Resultado esperado |
| --- | --- | --- |
| 1 | Activar demo y crear una categoría con el autollenado | Categoría DEMO disponible en administración |
| 2 | Nuevo producto → Regalo con stock → Completar campos → Guardar borrador | Producto privado con fotografía, precio de ejemplo y cero unidades |
| 3 | Abrir Más → Ver stock → completar y registrar Entrada | Físico 10, reservado 0, disponible 10; historial con responsable |
| 4 | Registrar Reserva de 2, Liberación de 2 y Salida de 2 | Disponible 8; cada cambio conserva saldos antes/después |
| 5 | Editar/publicar el producto y recorrer Productos, vista rápida, detalle y Galería | Aparecen producto, fotografía, precio y disponibilidad públicos |
| 6 | Contacto → Completar campos → Enviar consulta | Confirmación al cliente y consulta nueva en el panel |
| 7 | Consultas → Leída → Guardar; luego Resuelta → Guardar | Estado visible actualizado; no cambia el stock |
| 8 | Archivar el producto y desactivar demo | El producto sale de la tienda, pero sus datos e historial se conservan |

La guía también está dentro del panel y enlaza directamente a las pantallas necesarias.

## Verificación realizada

Suite completa: **50 pruebas, 0 fallos, 0 errores** (`mvn test`). Sintaxis JavaScript comprobada con Node. El flujo HTTP de integración usa H2 y controla la persistencia y los resultados entre módulos.

| Integración | Evidencia |
| --- | --- |
| Categorías → productos | Creación mediante formularios y categoría asociada; rechazo al borrar una categoría utilizada |
| Carga de fotografías → producto → detalle público | Imagen PNG decodificada, subida al servicio, recuperada públicamente y asignada al producto publicado |
| Borrador → publicación → catálogo/galería | Borrador devuelve 404 en detalle; publicación aparece en categoría, precio y disponibilidad |
| Stock → reservas → disponibilidad | Entrada, reserva, liberación y salida vía HTTP con versiones válidas; físico ocho y reservado cero al terminar |
| Movimientos → historial | Cuatro registros conservados y balances coherentes; regresiones de límites y formularios desactualizados |
| Contacto → consultas administrativas | Consulta creada públicamente, visible en panel, marcada leída y resuelta; stock sin cambios |
| Archivo → ocultamiento público → reactivación | Detalle/galería ocultan el producto archivado; se conserva el stock; se restaura la publicación |
| Duplicado | Pruebas previas comprueban nuevo código, borrador y ausencia de stock/reservas heredadas |
| Seguridad | Autenticación, ADMIN/STAFF, CSRF, cargas inválidas y notas internas; demo no omite validaciones |
| Sesiones demo | Activación explícita, separación por sesión, desactivación, cierre de sesión y configuración de apagado global |
| Inicio | Indicadores consultados sin cargar entidades completas, incluida la regresión de rendimiento previa |

Comprobación visual en navegador contra la instancia aislada de H2 en 8081: activar demo, autollenar producto con stock y guardarlo como borrador, registrar una entrada, crear una consulta desde la tienda y resolverla en el panel, crear categoría con autollenado y probar el producto por encargo seleccionando esa categoría. Se verificó que el llenado por sí solo no registra el stock ni envía consultas. Capturas locales: `demo-autollenado.png` y `demo-campos-completos.png`.

## Persistencia y alcance

Este modo **no crea una base aislada**: al guardar en la instalación habitual se escribe en el MySQL actual. El banner lo indica. Los ejemplos llevan DEMO para identificarlos; apagar el modo no borra registros. Para una exposición completamente descartable, utilizar una copia de la base y del almacenamiento de imágenes.

Las verificaciones automatizadas y visuales de esta sesión se realizaron con H2; no se han añadido ejemplos al MySQL del usuario ni se ha reiniciado su instancia en 8080. No se verificó aquí la actualización del esquema sobre la base MySQL existente. No hay cambios de esquema adicionales propios del modo demo. Los enlaces sociales se mantienen como enlaces externos; no son integraciones de mensajería o publicación. El catálogo permite consultas, no compra, pago ni reserva automática. El contacto aún es una consulta de texto libre y no genera un pedido asociado al producto.

No se afirma cobertura de todos los posibles datos de entrada: los recorridos principales y sus restricciones están cubiertos por la suite y las comprobaciones descritas.
