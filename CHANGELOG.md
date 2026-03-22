# CHANGELOG

Todos los cambios notables en el actual proyecto se documentarán en este fichero.
El formato está basado en [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
y este proyecto se ajusta a [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Cada versión debería:  
- Indicar su fecha de lanzamiento en el formato anterior.
- Agrupar los cambios para describir su impacto en el proyecto, de la siguiente manera:
    - `Added` para funcionalidades nuevas.
    - `Changed` para los cambios en las funcionalidades existentes.
    - `Deprecated` para indicar que una característica está obsoleta y que se eliminará 
       en las próximas versiones.
    - `Removed` para las características en desuso que se eliminaron en esta versión.
    - `Fixed` para correcciones y bugs.
    - `Security` para invitar a los usuarios a actualizar, en el caso de que haya vulnerabilidades.


## [Unreleased]
### Added
- Migrar contenidos de las lecciones, casos clínicos, etc de github a supabase.
### Refactor
- 
### Fix
- Corregir el crash cuando hay un tiempo de inactividad y hay una cancelación/renovadión 
  del token. En principio era un bug del ChannelFlow en Supabase 3.3.0 que debería estar
  corregido en la versión actual 3.4.1


## [0.10.0] - 2026-03-22
### Added
- Adecuado viewmodel para que durante sincronización se descarga markdown y localmente se 
  almacene como texto en Room.
- MarkdownText ahora renderiza desde el texto almacenado en Room, evitando doble descarga.
- Disminuye tamaño de letra en el renderizado de MarkdownText.
### Refactor
- Migrados los datos de github a supabase quedando esta última como única fuente de verdad
  remota.

## [0.9.0] - 2026-03-21
### Refactor
- Finaliza limpieza y refactorización de todas las features.
### Fix
- Adiciona el degradado a las pantallas de autenticación que nos lo tenían:
  - Asegura con una capa semitransparente, que se pueda ver correctaamente los textos.
  - Garantiza que al pasar de una pantalla a otra en auth, le header no se mueva y por
    tanto no se muestren saltos.

## [0.8.1] - 2026-03-20
### Refector
- Optimiza imports
- Realiza refactorización de la feature Chat, extrayendo strings y dimensiones a la carpeta de
  recurso del módulo :designsystem.
- Extrae componentes reutilizables al correspondiente paquete components haciendo los ficheros
  más pequeños y legibles.

## [0.8.0] - 2026-03-20
### Added
- Implementa toda la logíca y la ui para el tema de Aplicación de Conocimientos.
  - Implementa casos de usos con su calificación y la pantalla para mostrársela al estudiante.
  - Implementa los enlaces externos a los recursos complementarios.
### Refector
- Optimiza imports
- Realiza refactorización de la feature Course, extrayendo strings y dimensiones a la carpeta de 
  recurso del módulo :designsystem.
- Extrae componentes reutilizables al correspondiente paquete components haciendo los ficheros 
  más pequeños y legibles.

## [0.7.0] - 2026-03-19
### Added
- Implementa ls autoevaluaciones para cada tema y su calificación. Muestra nota al usuario
  en una pantalla completa que mantiene el estilo con fondo degradado,etc.

## [0.6.0] - 2026-03-17
### Added
- Implementa separación de chat según el tema:
  - Mantiene chat general
  - Cada TopicCard cuenta con un botón flotante que conduce al chat específico del
    tema.

## [0.5.0] - 2026-03-17
### Added
- Implementa Onboarding profesional:
  - Fondo degradado.
  - Una única pantalla per que hace uso de un Horizontal Pager para moverse de una pantalla
    a otra del onboarding con un simple swap
  - Elimina las pantallas previas que ya no son necesarias.
### Fix
- Elimina duplicidad del label con el nombre de la app en el manifest y además
  mejora el Deep Link en este fichero para eliminar los warnings que mostraba.

## [0.4.0] - 2026-03-17
### Added
- Implementa la feature Curso:
  - Lista de temas con su imagen, título, descripción, índice de 
    lecciones y avance
### Change
- Extraidas las dimensiones y las cadenas hardcodeadas a los recuerso 
  de módulo :designsystem
### Fixed
- Eliminados deprecados en todo el proyecto

## [0.3.0] - 2026-03-13
### Added
- Completado todo el flujo de autenticación:
  - Autenticación email/contraseña completa, incluyendo nombre completo
  - Autenticación con cuenta google completa, se utiliza el nombre, el avatar, etc
  - Autenticación anónimo (invitado) completa, implementado su transformación a una
    cuenta normal vinculándola a cuenta de google o a una con email y contraseña.
  - Enriquecido el flujo del registro, ahora incluye nombre completo
- Enriquecida al pantalla Profile:
  - Muestra foto de perfil, nombre completo, email
  - Separa en secciones:
    - Cuenta: Vincular la cuenta actual si es de invitado y opción de cerrar sessión
    - Asitencia: Abrir el chat de ayuda
    - Sobre NefroPed: Información de la versión de la app
### Refactor
- Restructura modularmente todos las definiciones referentes a la inyección de dependencias
  con Koin:
  - Modulo koin para los casos de uso vive ahora en :domain
  - Modulo koin para los repositorios ahora viven en :data
  - Modulo koin para los viewmodels ahora viven cada uno en su correspondiente feature 
  - El módulo que incluye todos los módulos sigue viviendo en :di

## [0.2.1] - 2026-03-10
### Added
- Implementa todo el flujo de recuperación de la contraseña en Supabase, dentro de la 
  app sin necesidad de web externa.
  - Utiliza la estrategía clásica en Supabase, envió de email con enlace (nefroped://reset-password) 
    que mediante el uso de deep link nos hace regresar a la app para indicar nueva contraseña
### Fix
- Suaviza la transición de pantallas durante el flujo de recuperación de la contraseña
  y para ello:
  - Implementa delay en MainActivity y demora de la splashscreen para dar tiempo a que
    se estabilice el estado se Supabase.
  - Remueve envoltura con AnimateAppEntry en cada uno de los NavGraph y envuelve
    todo el when con AnimatedContent()
- Evita la utilización de autocompletado al regreso a la pantalla de nueva contraseña.

## [0.2.0] - 2026-03-07
### Added
- Splash Screen utilizando androidx-core-splashscreen
- Ligera animación a primera pantalla que se muestre después de la splashscreen.
  En principio se aplica al navgraph del mundo en el que entre la aplicación 
  excepto la splashscreen.

## [0.1.0] - 2026-03-06
### Added
- Migración completa del proyecto ChatCleanApp hacia una arquitectura CLEAN modular:
  - Auntenticación elementar (login email/contraseña, anonimo y registro)
  - Chat general bastante profesional desde el punto de vista visual, realtime, con diferenciación
    entre usuario registrado y usuario invitado. Este último solo puede enviar 5 mensajes
    y pierde el historial si cierra la sesión.
