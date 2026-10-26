# Fotografías de productos Deluxe Eterno

Clasificación visual de 37 fotografías entregadas por el usuario. Los nombres comerciales y descripciones son propuestas editoriales para presentar la galería, no denominaciones oficiales ni compromisos de composición o disponibilidad.

## Organización

| Carpeta | Fotografías |
| --- | ---: |
| flores-y-arreglos | 6 |
| ramos-tematicos | 6 |
| boxes-con-personajes | 6 |
| regalos-motor | 9 |
| regalos-personalizados | 4 |
| boxes-romanticos | 1 |
| boxes-de-cuidado | 5 |

35 propuestas individuales, 1 vista alternativa y 1 fotografía de colección. No se encontraron duplicados binarios.

- **Ruta de Aventuras** tiene dos fotografías del mismo conjunto: la segunda termina en `-vista-02.jpg` y comparte ID DE-016. Presentarlas juntas como imágenes del mismo producto.
- **Pequeños Grandes Detalles** muestra varios minirramos. Usarla como colección o fotografía de categoría, no como la composición de un solo producto.
- Los nombres de personajes y equipos se emplean para describir temáticas visibles; no se afirma que exista una asociación comercial con esas marcas.

## Archivos de consulta

- `index.html`: catálogo visual local con selector de categorías y acceso a cada fotografía.
- `catalogo-productos.json`: metadatos para integrar después en la aplicación.
- `catalogo-productos.csv`: inventario editable con nombres originales, nombres nuevos, descripción, texto alternativo, dimensiones, categoría y SHA-256.

Los archivos JPG se movieron y renombraron sin recortar, comprimir ni modificar sus píxeles. Las huellas SHA-256 se comprobaron antes y después. La correspondencia permite recuperar los nombres originales si fuera necesario. Las hojas de contacto originales se conservan en `../docs/revision-productos/`.

La carpeta todavía no está conectada a las vistas ni a la base de datos. Las rutas de imagen de este inventario son relativas a Productos, no URLs públicas. No se asignaron precios ni disponibilidad. Antes de integrar, conviene exportar copias web con tamaños uniformes y conservar estos JPG como originales.
