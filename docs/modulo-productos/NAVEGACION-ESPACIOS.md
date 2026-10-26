# Navegación entre tienda y administración

Implementado el 26 de octubre de 2026.

## Accesos para el administrador

Al iniciar sesión como ADMIN, la tienda muestra una franja discreta bajo el menú principal con **Ir al panel** y una acción contextual. Se mantiene visible aunque el menú móvil esté cerrado.

| Pantalla pública | Acción administrativa |
| --- | --- |
| Inicio, catálogo y galería | Gestionar catálogo |
| Catálogo/galería filtrados | Gestionar catálogo con la misma categoría |
| Ficha de producto publicado | Editar este producto |
| Contacto | Ver consultas |
| Nosotros y servicios | Ir al panel |

En administración se añade **Ver en la tienda** al comienzo de todas las pantallas. El enlace del menú lateral también se adapta: edición/inventario de un producto publicado vuelve a su ficha; un borrador o producto archivado vuelve al catálogo. Categorías vuelve a la sección correspondiente del inicio y Consultas a Contacto. Las listas de productos conservan la categoría al pasar a la tienda. En Más opciones de cada producto publicado se añade Ver en tienda.

Los enlaces utilizan la misma pestaña y sesión: no requieren volver a iniciar sesión mientras esta siga vigente. No existe historial persistente de navegación ni un mecanismo de retorno arbitrario; el destino se calcula a partir de la sección actual. Los filtros de categoría conservados usan únicamente identificadores numéricos conocidos por la navegación.

## Permisos y verificación

La franja pública se renderiza exclusivamente cuando el servidor reconoce el rol ADMIN. Visitantes y STAFF no la ven. No se cambian las reglas de autorización del panel. Las rutas son internas y respetan el context path; no se toman destinos de Referer ni de parámetros de redirección.

Suite: **54 pruebas aprobadas**, incluyendo visibilidad por rol, acciones contextuales, categoría de ida y vuelta, retorno seguro de borradores y enlaces con context path. Se mantiene la regresión de no cargar productos completos en el resumen administrativo.

Revisión visual con H2 aislado en 8081: iniciar sesión, pasar del panel a la tienda, abrir una ficha pública, acceder directamente al editor y regresar a la misma ficha sin autenticarse de nuevo. Se corrigió la distribución de la cabecera para que la franja ocupe una fila independiente y no comprima el menú. La medición de cabecera del inicio incluye automáticamente esta nueva fila. Captura local: navegacion-admin-tienda.png (ignorada por Git).

No se modifica el esquema ni registros MySQL. Reiniciar Spring Boot con su configuración habitual para cargar los nuevos modelos/controladores de vista y recargar el navegador.
