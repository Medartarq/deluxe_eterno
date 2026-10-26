# Entorno demo independiente

## Uso local

El entorno MySQL demo ya está creado en esta instalación. Desde la carpeta del proyecto:

```powershell
.\scripts\demo.ps1 -Action Start
```

Abrir http://localhost:8081. La tienda habitual puede seguir abierta en http://localhost:8080. Para detener la demo, Ctrl+C en la terminal que la inició.

El administrador de este entorno es `admin-demo`. La contraseña generada se encuentra en `DEMO_ADMIN_PASSWORD` dentro de `.env.demo.local`. Ese archivo está ignorado por Git y contiene únicamente credenciales del entorno demo; no guarda la contraseña root utilizada para prepararlo. No publicar su contenido.

## Separación de entornos

| Elemento | Habitual | Demo |
| --- | --- | --- |
| Perfil Spring | Predeterminado | `demo` |
| Base MySQL | `deluxe_eterno` | `deluxe_eterno_demo` |
| Cuenta MySQL de esta instalación | Configuración habitual | `deluxe_demo_local@localhost` |
| Puerto HTTP predeterminado | 8080 | 8081 |
| Cookie de sesión | `JSESSIONID` | `DELUXE_DEMO_SESSION` |
| Cargas de imágenes | `uploads` | `uploads-demo` |
| Cuenta administradora | Usuarios habituales | `admin-demo` |

La demo escucha en 127.0.0.1 para uso local. Reutiliza las fotografías estáticas del proyecto como recursos de lectura, pero las fotos nuevas se escriben en su propia carpeta. Las contraseñas administrativas se almacenan en MySQL mediante BCrypt.

El perfil se elige al iniciar la aplicación, no mediante un botón del navegador. La precarga no copia registros de la base habitual. El botón **Activar autollenado** sigue completando formularios de la sesión, y no cambia de base ni guarda automáticamente. El aviso **Entorno demo** identifica el almacenamiento independiente y permanece visible aunque el autollenado esté apagado, también en el acceso de administración.

## Datos iniciales

- 35 productos: 33 publicados, un borrador y un archivado, en siete categorías con productos y una categoría vacía.
- Precios a consultar, fijos y desde; regalos por encargo y productos por unidades.
- Productos disponibles, con stock bajo, agotados y temporalmente no disponibles.
- Descripciones, contenido, personalización, preparación, ocasiones y notas internas.
- Ruta de Aventuras conserva sus dos fotografías de referencia.
- Seis consultas ficticias: dos nuevas, dos leídas y dos resueltas, con correos `example.test` y sin datos personales reales.
- Nueve movimientos: tres entradas, dos reservas, dos salidas, una liberación y una corrección. Cada movimiento tiene responsable, motivo y saldos coherentes.

| Producto / código | Físico | Reservado | Disponible | Escenario |
| --- | ---: | ---: | ---: | --- |
| Rayito de Ternura / DEMO-DE-001 | 10 | 2 | 8 | Disponible y con reservas |
| Snoopy Primavera / DEMO-DE-002 | 2 | 0 | 2 | Stock bajo y corrección por conteo |
| Kitty Sol de Girasoles / DEMO-DE-003 | 0 | 0 | 0 | Agotado, con reserva liberada y salida |
| Pollito de Mi Corazón / DEMO-DE-004 | — | — | — | Borrador |
| Pole Position / DEMO-DE-005 | — | — | — | Archivado |
| Acelerando Ilusiones / DEMO-DE-006 | — | — | — | Por encargo, precio desde |
| Parada Dulce / DEMO-DE-007 | — | — | — | Temporalmente no disponible |

Los precios y disponibilidades son ficticios. No se incorporan pedidos, pagos ni otras funciones que la aplicación aún no implementa.

## Reiniciar y restablecer

Reiniciar **conserva** los cambios: la versión del conjunto precargado queda registrada y no se sobrescriben ediciones, usuarios, cantidades ni consultas.

Para recuperar los datos iniciales:

1. Detener la instancia demo con Ctrl+C.
2. Ejecutar:

```powershell
.\scripts\demo.ps1 -Action Reset
.\scripts\demo.ps1 -Action Start
```

Reset elimina productos, categorías, consultas, movimientos y cuentas de **la base demo**, y recupera la precarga y su cuenta administradora con las credenciales del archivo local. Se mantienen el esquema y la identificación de la base. El borrado y la precarga se realizan dentro de una única transacción. Si hay un fallo, se revierten los cambios de datos. No se reinician los identificadores autoincrementales: los códigos de catálogo son estables, los ID numéricos pueden cambiar.

El comando rechaza el restablecimiento mientras el puerto demo configurado está ocupado. Para esta operación arranca brevemente una instancia local en un puerto aleatorio y se cierra al terminar; nunca se cambia el servidor habitual. No se borran archivos de imágenes subidas: permanecen en uploads-demo, aunque la precarga restaurada solo referencia recursos estáticos.

Para consultar la versión y fecha de inicialización:

```powershell
.\scripts\demo.ps1 -Action Status
```

## Colaboradores y configuración

Cada colaborador necesita MySQL 8, Java 26, Maven y el cliente `mysql` disponible en PATH. Después de clonar el código:

```powershell
.\scripts\demo.ps1 -Action Initialize
.\scripts\demo.ps1 -Action Start
```

Initialize solicita la contraseña MySQL local sin guardarla, crea únicamente `deluxe_eterno_demo`, configura el usuario limitado y genera credenciales locales. Se puede especificar `-RootUser` y `-MySqlPort`. Ejecutarlo otra vez conserva la versión y los registros de la demo. Si encuentra tablas sin la identificación del proyecto o una cuenta preexistente sin sus credenciales locales, se detiene para no apropiarse de datos o cuentas ajenos.

No es necesario compartir volcados SQL ni contraseñas por GitHub: los escenarios se generan desde `catalog/products.properties` y el código de precarga. Cada equipo obtiene sus propias credenciales. Los ajustes locales permitidos están en `.env.demo.local`: `DEMO_DB_URL`, `DEMO_DB_USERNAME`, `DEMO_DB_PASSWORD`, `DEMO_ADMIN_USERNAME`, `DEMO_ADMIN_PASSWORD` y `DEMO_PORT`. El nombre de la base y el usuario limitado se validan en el script. Si cambias la contraseña administrativa en el archivo, la cuenta ya existente no se modifica al reiniciar: aplica el restablecimiento explícito o un procedimiento de cambio de contraseña.

El perfil `application-demo.yml` usa variables DEMO independientes de DB_URL/DB_PASSWORD. El script guarda y restaura las variables de proceso para no dejar activado el perfil demo en la terminal tras detenerlo.

## Protecciones y verificación

Antes de que Hibernate modifique tablas, `DemoDatabaseGuard` comprueba el catálogo real de la conexión y la identificación `deluxe-eterno-demo` en `demo_environment`. El mismo control se ejecuta antes de precargar o restablecer datos. La cuenta MySQL solo tiene permisos sobre `deluxe_eterno_demo.*`; no se utiliza root para ejecutar la aplicación.

La precarga inicial y su versión se guardan juntas dentro de una transacción. Una versión distinta exige restablecimiento explícito; no se reemplazan datos automáticamente. Los inicializadores normales del catálogo y administrador quedan excluidos del perfil demo.

Validaciones realizadas en esta instalación:

- Suite completa: **59 pruebas aprobadas**, incluidas precarga, idempotencia, restablecimiento, saldos, cuenta BCrypt y rechazo de la base principal o de una identificación incorrecta.
- Arranque real con MySQL **8.0.46**, creación del esquema y precarga desde cero.
- Acceso real al panel como admin-demo y edición de un producto mediante el formulario.
- Reinicio real conservando el nombre editado, el stock y las reservas.
- Restablecimiento real recuperando Rayito de Ternura y su saldo 10 físico / 2 reservado / 8 disponible; conjunto final comprobado de 35 productos, 8 categorías, 6 consultas y 9 movimientos.
- Reejecución de Initialize sin eliminar datos existentes.
- `SHOW GRANTS` verifica permisos solo en el esquema demo. Un intento de lectura de products en deluxe_eterno con el usuario demo fue rechazado por MySQL (ERROR 1142).
- Cookies comprobadas por HTTP: JSESSIONID en 8080 y DELUXE_DEMO_SESSION en 8081. La instancia habitual no se detuvo ni se modificaron sus registros.
- `.env.demo.local` y uploads-demo están ignorados por Git. La contraseña root no se escribió en archivos del proyecto.

Captura local: `mysql-demo-panel.png` (ignorada por Git). Los logs de verificación también se mantienen fuera de Git.
