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

- Completar el ciclo de autenticación:
  - En el registro solicitar nombre de usuario como encajar esto en un anónimo
    para que se difrencie de otros anónimos.
  - Recuperar contraseña
  - Utilizar cuenta de google
  - Mejorar la interfaz gráfica parecida a NefroPediatra

- Implementar el feature Curso como está en NefroPediatra pero
  utilizando Supabase y aplicando CLEAN a profundidad.

### Fix 

- Corregir el crash cuando hay un tiempo de inactividad y hay una cancelación/renovadión 
  del token. En principio era un bug del ChannelFlow en Supabase 3.3.0 que debería estar
  corregido en la versión actual 3.4.1


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
