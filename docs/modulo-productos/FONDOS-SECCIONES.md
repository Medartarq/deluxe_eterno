# Fondos de categorías y servicios

Generados con la herramienta integrada imagegen. Integrados en las secciones Categorías y Servicios de la página de inicio mediante home.css. Se aplican desenfoque decorativo, capas claras centrales y tarjetas blancas para conservar legibilidad. En móvil aumenta la cobertura de la capa clara.

- Categorías: src/main/resources/static/assets/brand/fondo-categorias-salvia.png. Marfil y salvia, contraste suave frente a la portada rosa.
- Servicios: src/main/resources/static/assets/brand/fondo-servicios-champan.png. Champán y durazno, contraste cálido frente al bloque de productos rosa.

## Prompts utilizados

### Categorías

Create one wide 16:9 landscape photographic BACKGROUND for the Categories section of Deluxe Eterno, a boutique flowers and personalized gifts website. No text, no lettering, no logos, no UI, no watermark. This must gently contrast with a preceding warm blush-pink floral hero: use a light cool ivory and desaturated pale sage palette (#F3F4EE, #E7EDE4), with restrained blush and muted coral accents from satin ribbons. Premium editorial still life, soft diffuse daylight, dreamy optical defocus and subtle bokeh, low contrast but perceptible depth, not flat white. Frame the extreme left and lower-left edges with softly blurred ivory tulips, eucalyptus/sage leaves and a small pale gift box with a coral ribbon; a few delicate floral silhouettes at the far right edge. Keep the central 75 percent and upper center almost entirely uncluttered, a pale softly textured ivory-sage surface with gentle light and shadow gradients, to sit behind a centered heading and seven white product-category cards. The background must remain calm, elegant, airy and visibly different in hue from a pink section, never dark or highly saturated. Full bleed opaque image, refined photographic realism, edge decoration should not invade the central content area.

### Servicios

Create one wide 16:9 landscape photographic BACKGROUND for the Services section of Deluxe Eterno, a boutique flowers and personalized gifts website. No text, no lettering, no logos, no UI, no watermark, no people. This comes after a pale pink product section and needs gentle but visible tonal contrast: warm champagne ivory (#F5EADB), pale peach (#F3D5C4), light oat linen, and small muted coral accents, not a predominantly pink image. Premium editorial atmosphere suggesting flowers, surprise breakfasts/picnics and personalized gifts. Place tasteful softly out-of-focus objects only at the outer borders: lower-left a cropped picnic breakfast arrangement with a small cream cup and a hint of pastry on natural linen, far lower-right elegant ivory gift wrapping with coral satin ribbon and a small peach/white floral arrangement. Use dreamy shallow depth of field, soft warm morning light, gentle blurred silhouettes and delicate shadows. Keep central 75 percent and upper center bright, empty and calm for a centered heading, three white service cards and a button. Give the empty center subtle warm material texture and depth, not flat white. Elegant cohesive boutique style, restrained decoration, low contrast for dark readable overlay text, full bleed opaque background.


## Comprobación de integración

Revisión visual en localhost:8080, sin modificar registros: fondos cargados, contraste y tarjetas legibles, navegación por anclas y alturas de sección conservadas. Capturas locales: categorias-fondo-integrado.png y servicios-fondo-integrado.png. Cambio exclusivo de CSS y recursos; no requiere migración ni cambios de lógica.
