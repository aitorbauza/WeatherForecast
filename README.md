# Estructura del Proyecto

El proyecto está organizado en una estructura de paquetes Java que sigue un patrón de arquitectura en capas con separación clara de responsabilidades. A continuación se detalla cada módulo y las clases que lo componen:

---

## Módulo: `controller`

Este módulo contiene los controladores que manejan las solicitudes y coordinan la lógica de negocio.

- **`WeatherController`**: Gestiona las solicitudes relacionadas con el clima, actuando como intermediario entre la interfaz de usuario y los servicios de datos meteorológicos.

---

## Módulo: `data`

Este módulo maneja la persistencia y el acceso a datos, tanto locales como remotos.

- **`DBHelper`**: Proporciona funcionalidades para interactuar con la base de datos local, maneja conexiones, consultas y actualizaciones.

---

## Módulo: `dto` (Data Transfer Objects)

Este módulo contiene objetos que encapsulan datos para transferirlos entre subsistemas.

- **`ForecastResponse`**: Encapsula la respuesta de pronóstico completo recibida de servicios externos o generada internamente.
- **`WeatherResponse`**: Encapsula los datos de respuesta relacionados con condiciones meteorológicas actuales.

---

## Módulo: `model`

Este módulo contiene las entidades principales que representan el dominio de la aplicación.

- **`CurrentWeather`**: Representa las condiciones meteorológicas actuales en una ubicación específica.
- **`DailyForecast`**: Modela el pronóstico del tiempo para un día completo.
- **`HourlyForecast`**: Representa el pronóstico meteorológico en intervalos por hora.
- **`OutfitImageMapper`**: Mapea condiciones meteorológicas a imágenes de prendas de vestir recomendadas.
- **`OutfitRecommendation`**: Encapsula una recomendación de vestimenta basada en condiciones climáticas.
- **`RoutePoint`**: Representa un punto geográfico en una ruta con información meteorológica asociada.
- **`SavedOutfitEntry`**: Modela una entrada guardada de un conjunto de ropa para condiciones específicas.
- **`UserPreferences`**: Almacena las preferencias del usuario relacionadas con la aplicación.
- **`WeatherCache`**: Implementa un sistema de caché para datos meteorológicos para mejorar el rendimiento.

---

## Módulo: `repository`

Este módulo implementa el patrón repositorio para abstraer el acceso a datos.

- **`PreferencesRepository`**: Gestiona la persistencia y recuperación de las preferencias del usuario.
- **`WeatherRepository`**: Maneja el acceso y almacenamiento de datos meteorológicos.

---

## Módulo: `service`

Este módulo contiene la lógica de negocio principal de la aplicación.

- **`ForecastProcessor`**: Procesa datos de pronóstico meteorológico para extraer información relevante.
- **`OutfitDisplayHelper`**: Proporciona funcionalidades para mostrar correctamente las recomendaciones de ropa.
- **`OutfitRatingHelper`**: Gestiona la calificación de recomendaciones de vestimenta.
- **`OutfitService`**: Servicio principal para generar y gestionar recomendaciones de ropa.
- **`WeatherDataProcessor`**: Procesa datos meteorológicos crudos para su uso en la aplicación.
- **`WeatherService`**: Servicio principal que proporciona acceso a datos meteorológicos.
- **`WeatherTranslator`**: Traduce términos y descripciones meteorológicas entre diferentes idiomas.

---

## Módulo: `ui`

### Submódulo: `forms`

Contiene actividades relacionadas con la autenticación.

- **`LoginActivity`**: Maneja la funcionalidad de inicio de sesión de usuarios.
- **`RegisterActivity`**: Gestiona el registro de nuevos usuarios en la aplicación.

### Submódulo: `outfit`

Contiene clases relacionadas con la visualización de recomendaciones de ropa.

- **`OutfitActivity`**: Actividad principal para mostrar recomendaciones de ropa.
- **`OutfitCustomizeAdapter`**: Adaptador para personalizar la visualización de conjuntos de ropa.
- **`OutfitViewModel`**: Modelo de vista que contiene la lógica de presentación para las recomendaciones de ropa.
- **`OutfitViewModelFactory`**: Fábrica para crear instancias de `OutfitViewModel`.

### Submódulo: `outfitcomparison`

Contiene clases para comparar diferentes conjuntos de ropa.

- **`OutfitComparisonActivity`**: Actividad que permite comparar diferentes recomendaciones de ropa.
- **`OutfitComparisonViewModel`**: Modelo de vista para la comparación de conjuntos.
- **`OutfitComparisonViewModelFactory`**: Fábrica para crear instancias de `OutfitComparisonViewModel`.

### Submódulo: `route`

Contiene clases para gestionar rutas y mapas.

- **`MapManager`**: Gestiona la visualización e interacción con mapas.
- **`RouteManager`**: Administra las rutas del usuario y su información asociada.
- **`RouteWeatherActivity`**: Actividad que muestra información meteorológica a lo largo de una ruta.
- **`WeatherRouteManager`**: Integra información meteorológica con rutas geográficas.

### Submódulo: `weather`

Contiene componentes de UI relacionados con la visualización de datos meteorológicos.

- **`DailyForecastComponent`**: Componente UI para mostrar pronósticos diarios.
- **`HourlyForecastComponent`**: Componente UI para mostrar pronósticos por hora.
- **`LocationDialogManager`**: Gestiona diálogos para selección y búsqueda de ubicaciones.
- **`WeatherActivity`**: Actividad principal que muestra información meteorológica.
- **`WeatherDisplayComponent`**: Componente UI para mostrar datos meteorológicos actuales.

---

## Módulo: `util`

Este módulo contiene clases de utilidad que proporcionan funcionalidades compartidas.

- **`TimeUtils`**: Proporciona utilidades para manipulación y formateo de fechas y horas.
- **`WeatherIconMapper`**: Mapea condiciones meteorológicas a iconos representativos.

---

## Componentes Adicionales

- **`LocationSuggestionTask`**: Implementa la funcionalidad de sugerencias de ubicación durante la búsqueda.
- **`NavigationManager`**: Gestiona la navegación entre diferentes pantallas de la aplicación.
- **`SettingsActivity`**: Actividad que permite al usuario configurar las preferencias de la aplicación.
- **`WeatherApplication`**: Clase principal de la aplicación que inicializa componentes esenciales.

---

# Descripción Funcional

Esta aplicación combina pronósticos meteorológicos con recomendaciones de vestimenta, permitiendo a los usuarios:

- Consultar condiciones meteorológicas actuales y pronósticos.
- Recibir recomendaciones de ropa basadas en las condiciones climáticas.
- Guardar y personalizar conjuntos de ropa para diferentes condiciones.
- Visualizar información meteorológica a lo largo de rutas planificadas.
- Comparar diferentes recomendaciones de vestimenta.
- Establecer preferencias personales para adaptar recomendaciones.

---

## Arquitectura

La arquitectura sigue un patrón **MVVM** (Model-View-ViewModel) para la capa de presentación, con **repositorios** para abstraer el acceso a datos y **servicios** para encapsular la lógica de negocio, proporcionando una aplicación **modular, escalable y mantenible**.
