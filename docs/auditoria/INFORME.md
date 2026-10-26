# Auditoría integral y refactorización de Deluxe Eterno

**Fecha:** 26 de octubre de 2026, America/Lima. **Revisión base:** `5a0d0439337344b630d797ffc6df0d1a370ebb60`. **Estado inicial:** árbol con modificaciones y archivos nuevos de trabajos anteriores; se conservaron. **Alcance autorizado:** diagnóstico, correcciones internas justificadas, validación funcional/visual e informe, siguiendo `D:/JC Estudio/Workspace/output/agente-evaluacion-refactorizacion.md`.

## 1. Resumen ejecutivo

Se completó la revisión de código propio, configuración, plantillas, recursos e instrumentos de mantenimiento inventariados en [COBERTURA.md](COBERTURA.md). Se corrigieron fallos de validación y recuperación, se redujeron lecturas innecesarias, se eliminaron formularios duplicados y se preparó la colaboración mediante GitHub. La construcción limpia terminó con **36 pruebas aprobadas, cero errores y cero omisiones**, y generó el JAR ejecutable.

El objetivo de operación fue aclarado por el usuario: **entrega académica local, con colaboración en GitHub**. No se cambió de framework, JDK objetivo, versión de dependencias ni esquema de persistencia. No se publicaron cambios, no se crearon commits, no se desplegó y no se modificó la base MySQL del usuario. La aplicación auditada se ejecutó de forma aislada con H2 en el puerto 8081.

La conclusión global es **cumple parcialmente** para este alcance: hay evidencia suficiente para los flujos probados y la construcción local, pero no equivale a certificación de seguridad, comprobación exhaustiva de navegadores, ni validación final contra MySQL. La primera ejecución remota del workflow, la comparación móvil exhaustiva, la paginación a gran escala y un escaneo completo de dependencias quedan documentados como pendientes.

## 2. Ficha técnica y contexto

| Elemento | Verificado |
| --- | --- |
| Lenguaje y runtime | Java, compilación objetivo 26. Línea base ejecutada con JDK 27; construcción limpia y servidor final con JDK 26. |
| Construcción | Maven 3.9.16 disponible; `pom.xml` usa Spring Boot parent 4.1.1. No existe Maven Wrapper. |
| Framework | Spring Boot 4.1.1, Spring MVC, Thymeleaf 3.1.5.RELEASE, Spring Data JPA 4.1.1, Hibernate 7.4.5.Final. |
| Seguridad | Spring Security 7.1.1, autenticación por sesión, BCrypt y roles ADMIN/STAFF; solo ADMIN entra al panel. |
| Persistencia | MySQL Connector/J 9.7.0; H2 2.4.240 para pruebas. `open-in-view=false`; `ddl-auto=update` por defecto local y `create-drop` en test. |
| Servidor | Tomcat embebido 11.0.24; aplicación monolítica renderizada en servidor. |
| Frontend | Bootstrap 5.3.3, Bootstrap Icons 1.11.3 vía jsDelivr; CSS y JavaScript propios, sin SPA ni gestor npm necesario para ejecutar el sitio. |
| Recursos | 35 productos iniciales, 37 fotografías contando alternativa y colección; fotos públicas suman 12.926.443 bytes. |
| Operación confirmada | Ejecución académica local; GitHub para colaboración. No hay contenedores, servidor de producción ni CI previo en el árbol inicial. |

La organización del sistema es un **monolito**; el mecanismo de ejecución es un servidor Java embebido; la arquitectura interna combina **capas y MVC**. Son conceptos compatibles. La evidencia no se basa solo en carpetas: `PublicController`/`AdminController` coordinan modelos y vistas, los servicios delimitan operaciones transaccionales, y los repositorios acceden a entidades JPA mediante interfaces Spring Data.

Variables documentadas, sin valores secretos: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `PORT`, `JPA_DDL_AUTO`. No se solicitó ni se leyó la contraseña de MySQL. La versión exacta del servidor MySQL local no se verificó en esta intervención; el README requiere MySQL 8 o superior.

## 3. Alcance y cobertura

La matriz [COBERTURA.md](COBERTURA.md) enumera cada paquete, las ocho vistas públicas, siete vistas administrativas/autenticación, fragmentos, errores, pruebas, generadores y configuración. Incluye las 25 rutas propias finales y los contratos gestionados por Spring Security.

Se revisaron los archivos Java y HTML propios y las fuentes CSS/JS. Los SVG se validaron como XML; los productos se agruparon por el inventario editorial y se verificaron las 37 huellas SHA-256 frente a originales y copias públicas. Se revisaron también los generadores Python/JS y los scripts históricos de Word/PowerPoint, sin ejecutarlos contra documentos del usuario. Los diagramas APF2 se leyeron como documentación histórica.

Se excluyeron el código de bibliotecas descargadas, cachés, binarios compilados y configuración privada. No se afirma que cada fotografía haya recibido una nueva auditoría de píxeles: se conservaron los encuadres previos y su integridad. `inventario-inicial.json` registra 106 archivos de fuente/configuración y recursos al comenzar; `inventario-final.json` enumera el árbol fuente final. Los logs, capturas y respaldo local están ignorados por Git deliberadamente.

## 4. Estado inicial y contratos preservados

La línea base `mvn test` terminó correctamente con **14 pruebas**. Las pruebas existentes verificaban páginas públicas, protección del panel, consulta válida, inclusión de JavaScript, modal, edición de producto inactivo, filtro de catálogo e importación idempotente. No cubrían varios caminos de error ni autenticación real con un usuario persistido.

Se preservaron las rutas existentes, sus nombres de vistas, la autenticación por formulario, los POST con CSRF, el filtrado por `categoria`, la baja lógica, las fotografías, el orden alfabético del catálogo y los primeros tres productos de inicio. La nueva ruta de publicación es aditiva. Se preservaron todos los productos administrados: la auditoría no aplicó cambios de datos a MySQL.

Antes de editar se guardaron `estado-inicial.txt`, `antes.zip`, los resultados de las 14 pruebas y capturas de inicio, contacto y alta de producto en una instancia aislada. Los estados de validación y errores se compararon después donde fue posible.

## 5. Hallazgos y resolución

Prioridad: alta = riesgo importante de pérdida o fallo; media = defecto funcional/robustez; baja = mantenimiento u operación proporcional. La certeza se refiere a la evidencia disponible, no a una estimación de vulnerabilidad universal.

| ID | Módulo y evidencia | Impacto / principio | Prioridad y certeza | Estado final |
| --- | --- | --- | --- | --- |
| H-001 | `CatalogService.createCategory/updateCategory` guardaban sin controlar duplicados ni normalizar márgenes; categoría tiene índice único | Flujo administrativo puede acabar en restricción DB; duplicados por variación de mayúsculas/espacios; SRP de reglas de negocio | Media, alta | **corregido**: comprobación sin distinguir mayúsculas, trim, excepción de negocio, `saveAndFlush` y error del campo conservando contexto |
| H-002 | `InquiryService.updateStatus` usaba `orElseThrow()` vacío | ID inexistente provocaba `NoSuchElementException` y error interno | Media, alta | **corregido**: HTTP 404 con mensaje controlado |
| H-003 | Baja lógica sin operación inversa ni acción en panel | Productos inactivos no podían volver al catálogo sin intervención en datos | Media, alta | **corregido**: POST `/admin/products/{id}/activate`, rol/CSRF existentes y botón Publicar solo para inactivos |
| H-004 | Inicio obtenía todos los productos y después limitaba en memoria; dashboard cargaba listas para contarlas | Transferencia e hidratación innecesarias; DAO/repo y rendimiento | Media, alta | **corregido y medido**: query Top3 y count; carga de productos 35 → 3 para inicio; dashboard 0 entidades Product/Category |
| H-005 | Dos formularios completos casi idénticos en cada plantilla de producto/categoría | Validación y marcado podían divergir entre alta/edición; mantenibilidad | Baja, alta | **corregido**: una estructura con acción y texto del botón condicionales |
| H-006 | `ProductForm.imagePath` solo tenía obligatoriedad/tamaño | Se aceptaban esquemas impropios como `javascript:`; recursos rotos y ambigüedad de entrada | Media, alta | **corregido**: patrón permite rutas locales absolutas y HTTP(S), conserva las URL externas legítimas; no se declara un XSS explotable por esa entrada |
| H-007 | Categoría eliminada o inexistente enviada por un formulario de producto | Error 404 fuera del formulario en vez de recuperación de selección | Media, alta | **corregido**: error de campo y lista de categorías conservada |
| H-008 | Selector de estado sin nombre accesible; acordeón sin aria-expanded/controls; página activa no marcada en subrutas administrativas | Accesibilidad básica y orientación | Baja, alta | **corregido en esos atributos**; auditoría WCAG/lector de pantalla **pendiente** |
| H-009 | `inspeccionar_productos.py` buscaba JPG solo en raíz; después de renombrar encontraba cero y sobrescribía el inventario histórico | Pérdida de trazabilidad de fotos al repetir herramienta | Alta para mantenimiento, alta | **corregido**: se detiene sin sobrescribir si ya están clasificadas o existe el inventario; clasificador usa JSON público si falta el histórico |
| H-010 | Generador de favicon requería Sharp desde una ruta personal fija | Colaboradores no podían resolver el módulo | Baja, alta | **corregido**: resolución normal o `SHARP_MODULE_PATH`, errores con salida no cero; generadores opcionales documentados |
| H-011 | `docs/` ignorado completamente y ausencia de CI/convenio de colaboración | Informe y herramientas no compartibles de forma normal; validación manual inconsistente | Baja, alta | **corregido localmente**: excepciones Git para informe/generadores, exclusión de evidencia/secretos, CONTRIBUTING y workflow; ejecución remota **pendiente** |
| H-012 | Sin páginas propias para errores HTML | Fallos mostraban experiencia genérica fuera de la identidad visual | Baja, alta | **corregido**: 404/500 sin detalles técnicos; 404 verificado por HTTP/navegador y 500 por fuente |
| H-013 | Suite original insertaba fixtures sin rollback por prueba | Acoplamiento por orden y contaminación entre escenarios | Baja, alta | **corregido**: transacción y rollback también en la suite original |
| H-014 | Listas de consultas/productos sin paginación; catálogo actual de 35 productos | Crecimiento de memoria/HTML con un volumen mucho mayor | Baja en alcance local, alta respecto al código | **pendiente por proporcionalidad**: no cambiar navegación pública en una auditoría que preserva producto; medir volumen real antes de diseñar paginación |
| H-015 | Bootstrap/Icons dependen de CDN; fotos mantienen resolución original y logos son reconstrucciones raster | Uso sin Internet y peso; calidad del maestro gráfico | Baja local, alta | **pendiente**: no se sustituyeron recursos ni se recomprimieron imágenes; JavaScript tolera falta de Bootstrap sin lanzar error en el cierre del menú |
| H-016 | Configuración local root, SSL desactivado en URL de ejemplo, update y caché Thymeleaf desactivada | No es perfil de producción | Baja para local, alta | **descartado como incompatibilidad local** por respuesta del usuario; requerir revisión antes de exposición pública |
| H-017 | Avisos oficiales Spring revisados frente a versiones resueltas | Riesgo de atribuir una vulnerabilidad sin verificar rango ni uso | Consulta acotada, alta para los cinco avisos | **descartado para esos cinco avisos**: JPA 4.1.1 y Security 7.1.1 son versiones corregidas; no es un SCA exhaustivo |

### Comprobación de los defectos materiales

La primera fase roja de `AuditRegressionTests` ejecutó ocho casos: cuatro pasaron y cuatro fallaron por motivos de negocio esperados. Duplicados/renombrado devolvían redirección en vez de validación; publicar devolvía 404 al no existir la ruta; actualizar una consulta inexistente lanzó `NoSuchElementException`. En pruebas transaccionales, el flush/commit puede diferirse; por ello no se atribuye el 302 observado a que el guardado duplicado fuera seguro en MySQL. La restricción única y la ausencia de tratamiento demostraban el camino de fallo al confirmar la operación.

Tras la primera corrección se añadieron el caso de categoría obsoleta y el esquema de imagen. El primero pasó; el segundo falló con 302 antes de incorporar el patrón. La fase verde posterior ejecutó 24 casos sin fallos. La ampliación de pruebas de error HTML necesitó enviar `Accept: text/html`; sin él Spring negociaba una respuesta sin ModelAndView. Fue una corrección de la prueba, no un defecto del controlador. La batería final pasó 36 casos.

La medición usa estadísticas de Hibernate habilitadas solo dentro de una prueba transaccional y deshabilitadas en `finally`. Con 35 productos se reprodujo el algoritmo previo y se comparó con GET de inicio: **35 frente a 3 entidades Product cargadas**. En el dashboard, Product y Category cargadas fueron cero. No se midió latencia de producción ni se afirma un porcentaje de mejora temporal.

## 6. Cambios realizados y compatibilidad

Se añadieron `DuplicateCategoryException`, dos plantillas de error, `AuditRegressionTests`, `CONTRIBUTING.md` y el workflow. Se modificaron los dos servicios de negocio, repositorios de catálogo, controladores, ProductForm, JavaScript y plantillas indicadas en `cambios.json`. Ese registro diferencia cambios de esta auditoría de cambios que ya existían frente a HEAD. No se eliminó código propio ni se sustituyeron imágenes.

Las operaciones de negocio permanecen en servicios; los controladores traducen conflictos recuperables a errores de campo. La restricción única sigue siendo la defensa final frente a una carrera entre altas; no se finge que la comprobación previa elimine la carrera. La baja de categoría conserva el chequeo de asociaciones y ahora presenta un mensaje seguro si la DB detecta una asociación concurrente.

La publicación reutiliza `Product.active`, por lo que no requiere esquema ni migración. Los conteos y Top3 no cambian el contenido esperado. La validación de imagen rechaza esquemas impropios conservando `/assets/...` y URL http(s). La nueva validación de categoría preserva los datos del formulario. La eliminación de formularios duplicados no cambia IDs, campos, botones ni distribución.

Las herramientas de exportación verifican que las rutas no salgan de sus directorios y que los SHA-256 coincidan antes de copiar. Se permitió versionar el informe y los generadores, pero se mantuvieron fuera de Git ZIP, logs, capturas y configuración privada. La documentación de marca se actualizó para reflejar el favicon ya existente.

## 7. Matriz de principios

Los estados siguientes se limitan al código y pruebas examinados.

| Principio | Alcance | Inicial | Final | Evidencia y decisión |
| --- | --- | --- | --- | --- |
| MVC | Aplicación HTML renderizada | **cumple** | **cumple** | Controladores devuelven nombres de vistas y modelos; reglas y transacciones en servicios. Las validaciones recuperables se coordinan en web sin SQL en plantillas. |
| DAO | Persistencia | **cumple parcialmente** como implementación literal | **cumple** como frontera de acceso a datos | Spring Data encapsula consultas y JPA; no se añadieron DAO que solo reenviaran llamadas a repositorios. |
| TDD histórico | Historial anterior | **no verificable** | **no verificable** | Tener tests no demuestra su orden de creación. |
| TDD intervención | Defectos de duplicado, ausencia, publicación y esquema | **no aplica** antes de intervenir | **cumple** en ciclos registrados | Fallo relevante confirmado antes del cambio y fase verde. Pruebas añadidas de caracterización/seguridad/rendimiento no se etiquetan como TDD histórico. |
| SRP | Controladores/servicios/vistas/herramientas | **cumple parcialmente** | **cumple parcialmente** | Reglas de duplicación en servicio, formularios unificados. AdminController conserva tres áreas de coordinación; dividirlo sin necesidad no aportaría un límite nuevo. |
| OCP | Puntos de variación reales | **cumple parcialmente** | **cumple parcialmente** | Catálogo ya extensible mediante properties y repositorios. No se diseñó un sistema de plugins hipotético ni una capa adicional. |
| LSP | Herencia/sustitución propia | **no aplica** | **no aplica** | No existe jerarquía propia con implementaciones sustituibles que justifique una evaluación de subtipos; no se deduce LSP de anotaciones Spring. |
| ISP | Contratos consumidos | **cumple parcialmente** | **cumple parcialmente** | Interfaces de repositorio del framework son amplias, pero no hay una interfaz propia que obligue a clientes a implementar métodos inútiles. Fragmentarlas no reduce un acoplamiento material. |
| DIP | Servicios frente a persistencia y web frente a negocio | **cumple** en el límite revisado | **cumple** en ese límite | Inyección por constructor y repositorios interfaz permiten sustitución. No se añadieron interfaces de servicio solo para justificar una etiqueta. |

## 8. Pruebas y validación

| Comando / comprobación | Contexto y resultado |
| --- | --- |
| `mvn test` inicial | JDK 27, H2: 14 aprobadas. Copias `base-publico.txt` y `base-catalogo.txt`. |
| `mvn '-Dtest=AuditRegressionTests' test` rojo | Ocho casos, tres fallos de aserción y un error esperado; `tdd-rojo.log`. |
| Mismo comando, ampliado | Diez casos, solo falla esquema de imagen antes de corregirlo; `tdd-intermedio.log`. |
| `mvn test` verde | 24 aprobadas; `tdd-verde.log`. |
| Ampliación de validación | 28 casos, una prueba de HTML sin cabecera Accept corregida posteriormente; `verificacion-ampliada.log`. |
| `mvn --batch-mode --no-transfer-progress clean verify` final | `JAVA_HOME` apuntando al JDK 26 instalado: **36 aprobadas**, JAR empaquetado; `verificacion-final.log`. |
| `mvn dependency:tree '-DoutputFile=docs/auditoria/dependencias.txt'` | Árbol real resuelto; sin modificaciones a dependencias. |
| `node --check src/main/resources/static/js/main.js` con runtime disponible | Sintaxis válida. No equivale a suite JS ni a ejecución en todos los navegadores. |
| AST Python, XML SVG y SHA-256 | Tres scripts Python válidos; nueve SVG válidos; 37 originales y copias coherentes. |
| Clasificación/exportación | Clasificador ejecutado: cero renombrados adicionales; exportador: 37 fotos y 35 productos; inspección se detiene preservando inventario existente. |
| `git diff --check` | Sin errores de espacios; advertencias de conversión LF/CRLF del entorno no son fallos de código. |

La batería final cubre los siete GET públicos generales, detalle, filtros, modal, permisos ADMIN/STAFF, CSRF, login BCrypt persistido, consulta inválida/no persistida, consulta válida, escape de HTML almacenado, cambio de estado, IDs inexistentes, CRUD de producto, baja y publicación, duplicación/renombrado/baja de categoría con asociaciones, idempotencia del catálogo, favicon, error HTML y carga de entidades. Las pruebas usan rollback. **No se midió cobertura de código con JaCoCo**; no se informa un porcentaje inventado.

No se ejecutó un scanner SCA completo, un benchmark de concurrencia, una prueba de restauración de backups de MySQL ni una ejecución remota de Actions. No se sustituyen esas pruebas por inspección de archivos.

## 9. Preservación funcional y visual

Se usaron los mismos 35 productos de semilla y el mismo navegador en capturas de **1265 × 712** antes y después. El resultado se registra en `comparacion-visual.json`:

- **Contacto:** captura idéntica píxel a píxel.
- **Inicio:** diferencia de 0,0009 % de píxeles con cambio de canal mayor que 10/255, limitada al enlace de navegación. Contenido y composición revisados sin regresión; no se afirma igualdad binaria.
- **Alta de producto:** diferencia de 0,091 % con el mismo umbral, limitada al menú administrativo. Es el estado activo de navegación corregido; el contenido del formulario conserva su distribución.

También se inspeccionaron capturas finales de Nosotros, Servicios, Galería, 404 y del error recuperable de categoría duplicada. `revision-paginas.jpg` reúne las pantallas públicas adicionales. El error de duplicación conserva el formulario y muestra feedback. El inicio/login se probaron realmente en el navegador aislado; un intento con campos autocompletados requirió limpiar sus valores, tras lo cual autenticó correctamente. No se atribuyó ese intento a una regresión del backend.

No se realizaron cambios al CSS, a las fotografías ni al logotipo durante esta auditoría. La apariencia nueva deliberada se limita al botón Publicar de productos inactivos, estado activo de navegación, feedback accesible y páginas de error. Los errores HTML son una mejora explícita; las respuestas JSON negociadas del framework se conservan.

La adaptación por puntos de corte y movimiento reducido se revisó en código. **No se volvió a verificar cada pantalla en 360/390 px, tablet física, Safari o lector de pantalla.** Las capturas de trabajos anteriores no se presentan como pruebas nuevas de esta auditoría.

## 10. Compatibilidad de despliegue y colaboración

El usuario confirmó que no habrá despliegue público por ahora. Los requisitos y comandos de ejecución normal no cambian. No se modificó `pom.xml`, `application.yml`, entidades JPA ni el esquema. No se ejecutaron migraciones. La app final se construyó y arrancó con JDK 26 y H2; MySQL del usuario y su proceso en 8080 no se reiniciaron ni se administraron desde esta auditoría. Para usar el código nuevo allí, reiniciar desde la terminal habitual que ya dispone de sus variables.

El workflow usa `actions/checkout@v7` y `actions/setup-java@v6`, Java 26, caché Maven, permisos `contents: read`, credenciales de checkout no persistentes y `clean verify` sin MySQL. Estas versiones se contrastaron con documentación oficial el día de la auditoría. **El archivo se revisó y el comando equivalente pasó localmente; el job no se ejecutó en GitHub**. Su activación requerirá subir los cambios al repositorio y tener Actions habilitado. No es un workflow de despliegue.

Las acciones utilizan etiquetas mayores mantenidas, no SHA inmutables. Fijar SHA y revisar actualizaciones sería un endurecimiento adicional razonable si las políticas del equipo lo requieren. Maven no tiene wrapper; se documentan runtime y Maven y se conserva el BOM de Spring Boot. No se instalaron cambios globales en equipos de colaboradores.

### Consulta de dependencias

Se contrastaron cinco avisos oficiales: [CVE-2026-47834 de Spring Data JPA](https://spring.io/security/cve-2026-47834/), [CVE-2026-41707](https://spring.io/security/cve-2026-41707/), [CVE-2026-47841](https://spring.io/security/cve-2026-47841/), [CVE-2026-47842](https://spring.io/security/cve-2026-47842/) y [CVE-2026-47877 de Spring Security](https://spring.io/security/cve-2026-47877/). Las versiones resueltas 4.1.1/7.1.1 figuran como corregidas. Además, el proyecto no usa consultas SQL nativas con Sort recibido del usuario, DPoP, WebAuthn, AES/CBC ni Authorization Server. Esto permite descartar **esos avisos concretos**, no todas las vulnerabilidades posibles de la dependencia o sus transitivas.

Fuentes operativas consultadas: [setup-java](https://github.com/actions/setup-java), [checkout](https://github.com/actions/checkout) y [avisos de Spring](https://spring.io/security/). No se actualizó masivamente ninguna dependencia como efecto secundario.

## 11. Decisiones de no intervención

- **DAO manual, interfaces de cada servicio o microservicios:** descartados por proporcionalidad. Los repositorios y la inyección actuales ya proporcionan separación útil; añadir delegadores no mejora reglas ni pruebas.
- **Separar AdminController por tamaño:** descartado como criterio aislado. Se conservaron coordinación y convenciones; una futura expansión de cada dominio sí podría justificar controladores independientes.
- **Migrar Java, Spring, MySQL o introducir Docker:** no necesario para el destino confirmado y aumenta riesgo de incompatibilidad académica.
- **Paginación y protección antiabuso de Internet:** registradas para crecimiento/exposición futura. No se añadió CAPTCHA, rate limiting distribuido ni navegación nueva a un proyecto local sin esos requisitos.
- **Índices, versionado optimista y migraciones de esquema:** sin medición de carga ni exigencia de concurrencia que justifique el cambio. Mantenerlos como decisión explícita, no como ausencia silenciosa de revisión.
- **Recomprimir fotos o sustituir logos:** se preservó el trabajo visual ya aceptado. La carga diferida existente reduce descargas inmediatas; optimización de peso requiere una comparación específica de calidad.
- **Cambiar misión/visión o material APF2 histórico:** el informe académico identifica propuestas institucionales, pero la auditoría no reescribe contenido comercial ni entregables ya presentados. La falta de aclaración en la página puede revisarse editorialmente si el equipo lo solicita.
- **Publicar, crear PR/commit o ejecutar el workflow remoto:** no solicitado; los archivos quedan revisables en el workspace.

## 12. Pendientes y reversión

Pendientes priorizados:

1. Reiniciar la instancia habitual y comprobar el panel contra MySQL con su configuración local. Las pruebas de esta auditoría permanecieron aisladas.
2. Subir los cambios revisados y observar la primera ejecución de GitHub Actions; acordar con colaboradores versión de Maven o incorporar wrapper si necesitan fijarla estrictamente.
3. Repetir revisión visual en móvil 360/390, tablet y lectores de pantalla; validar estados de error y tabla administrativa con teclado.
4. Ejecutar un SCA integral periódico, comprobar el acceso al CDN si la exposición académica es sin Internet y planificar optimización de fotos sin degradación.
5. Antes de crecer o publicar: paginación, requisitos de concurrencia, perfiles de producción, TLS, cuenta MySQL de privilegios mínimos, migraciones y política de backups. No son requisitos implícitos de la entrega local actual.

Para revertir **solo esta auditoría**, usar `cambios.json` y el respaldo local `antes.zip`: extraer cada archivo modificado a una carpeta temporal, comparar el archivo actual y restaurar únicamente las rutas correspondientes cuando no tengan trabajo posterior. No restaurar el ZIP entero sobre el proyecto y no usar `git reset --hard`, porque el árbol ya tenía cambios locales. El ZIP contiene la versión de inicio de `src`, `pom.xml`, README y `.gitignore`, no un snapshot del commit remoto.

Las seis nuevas fuentes/configuraciones se identifican en `cambios.json`; eliminar solo las creadas por esta intervención si se revierte y no han recibido cambios posteriores. Los generadores auxiliares no formaban parte del ZIP de fuente inicial: sus modificaciones son pequeñas y están descritas en H-009/H-010 y en la sección de cambios; revisar esos bloques antes de deshacerlos, sin regenerar ni renombrar fotografías. No hay migración que revertir ni datos MySQL modificados por la auditoría.

**Resultado final:** trabajo de revisión e implementación completado para el alcance local inventariado, con 36 pruebas y empaquetado correctos. Las verificaciones no ejecutadas mantienen explícitamente su estado; no se declara ausencia total de defectos ni equivalencia visual universal.
