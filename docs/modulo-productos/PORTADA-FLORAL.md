# Portada floral y navegación por secciones

26 de octubre de 2026.

## Resultado

Se integra un fondo fotográfico de flores y cajas de regalo en tonos rosa, coral y crema. La imagen se muestra como fondo decorativo con desenfoque CSS y una capa clara para preservar la lectura; el logotipo permanece como elemento independiente.

Recurso: `src/main/resources/static/assets/brand/hero-floral-blur.png` (1672 × 941 píxeles, aproximadamente 1,7 MB). Generado con la herramienta integrada imagegen, sin API/CLI externo. Se conserva el original generado y una copia dentro del proyecto. No se alteraron los logotipos existentes.

La página de inicio dispone de cuatro secciones identificadas: inicio, categorías, destacados y servicios. Sus alturas mínimas usan el alto de pantalla menos la cabecera medida. Se incluye desplazamiento nativo CSS Scroll Snap y una navegación lateral por anclas con estado activo. No se interceptan eventos de rueda ni se bloquea el teclado. El pie de página sigue siendo accesible.

En escritorio con ancho mínimo de 992 px y alto mínimo de 680 px, el ajuste entre secciones es obligatorio; para pantallas intermedias es de proximidad. En móviles pequeños y con preferencia de movimiento reducido se mantiene desplazamiento libre. Las secciones pueden crecer si el contenido lo requiere; no se recortan categorías o textos para forzar una altura fija. Al abrir una vista rápida se suspende el ajuste y se restablece al cerrarla.

Se añade CSS exclusivo `home.css` a la portada y lógica de medición/estado en `home.js`. Las páginas internas y el panel mantienen sus estilos.

## Verificación

- Suite Maven: 50 pruebas, cero fallos y cero errores.
- Sintaxis de `home.js` comprobada con Node.
- Revisión visual en instancia H2 aislada en 8081: portada, categorías, destacados, servicios y apertura/cierre de vista rápida.
- A 720 px de alto, cabecera de aproximadamente 87 px y cuatro secciones de 633 px: títulos y acciones visibles dentro de cada bloque con las siete categorías actuales.
- Comprobado desplazamiento con rueda, accesos por anclas e indicador lateral activo; el modal suspende Scroll Snap.
- Reglas adaptables revisadas; no se probó en un dispositivo móvil físico.
- Captura local integrada: `inicio-fondo-floral.png` (ignorada por Git).

## Prompt utilizado

Create a single wide landscape photographic website hero BACKGROUND image for Deluxe Eterno, a boutique floral and personalized gift shop. No lettering, no logos, no UI, no watermark. Soft romantic editorial still life of blush roses, ivory tulips, a few warm yellow flowers, elegant pale pink gift boxes with coral satin ribbon, on a light warm ivory surface with soft natural window light. Intentionally artistically defocused with gentle optical blur and subtle bokeh, recognizable floral and gifting silhouettes rather than sharp product photography. Warm blush pink #FFF0F5, muted coral #EF6669, cream and subtle sage. Composition: keep the left 55 percent mostly open pale ivory/blush negative space, floral bouquet and ribbons concentrated along far right and lower-right edge, softly receding background. Quiet premium welcoming mood, tasteful restrained styling, bright airy exposure, low contrast suitable behind dark readable website text. Wide 16:9 composition, full bleed opaque background.
