# WEARTHER - AITOR BAUZÁ GÓMEZ

## Descripción General
Esta aplicación proporciona pronósticos del tiempo y recomendaciones de vestimenta basadas en condiciones meteorológicas. Permite a los usuarios consultar el clima actual, previsiones horarias y diarias, y recibir sugerencias de vestimenta apropiada. Además, incluye funcionalidades para planificar rutas considerando las condiciones meteorológicas.

---

## Estructura del Proyecto

**Paquete raíz:** `com.example.weatherforecast`

### controller
Maneja las solicitudes de los usuarios y coordina la comunicación entre la interfaz y las capas de servicio.

- `WeatherController`: Gestiona las peticiones relacionadas con el clima, procesa entradas del usuario y devuelve respuestas formateadas.

### data
Gestiona el acceso y persistencia de datos.

- `DBHelper`: Facilita la conexión y operaciones con la base de datos.
- `WeatherCache`: Implementa un sistema de caché para almacenar temporalmente datos meteorológicos y reducir llamadas a API externas.

### dto (Data Transfer Objects)
Objetos para transferir datos entre subsistemas de la aplicación.

- `ForecastResponse`: Encapsula los datos de pronóstico para transferir entre capas.
- `WeatherResponse`: Estructura los datos de respuesta de clima actual para presentarlos al usuario.

### model
Define las entidades principales del dominio de la aplicación.

- `CurrentWeather`: Representa los datos del clima actual para una ubicación específica.
- `DailyForecast`: Modela pronósticos meteorológicos por día.
- `HourlyForecast`: Contiene previsiones meteorológicas por hora.
- `OutfitRecommendation`: Representa sugerencias de vestimenta basadas en condiciones climáticas.
- `RoutePoint`: Define puntos geográficos para planificación de rutas.
- `SavedOutfitEntry`: Almacena combinaciones de vestimenta guardadas por el usuario.
- `UserPreferences`: Contiene ajustes y preferencias configuradas por el usuario.

### repository
Implementa el patrón repositorio para abstraer y encapsular la lógica de acceso a datos.

- `PreferencesRepository`: Maneja operaciones CRUD para las preferencias de usuario.
- `WeatherRepository`: Gestiona el acceso a datos meteorológicos, ya sea desde caché local o API externa.

### service
Contiene la lógica de negocio principal de la aplicación.

- `ForecastProcessor`: Procesa datos brutos de pronóstico para extraer información relevante.
- `OutfitDisplayHelper`: Ayuda a presentar recomendaciones de vestimenta en la interfaz.
- `OutfitService`: Proporciona recomendaciones de vestimenta según condiciones climáticas.
- `WeatherDataProcessor`: Procesa datos meteorológicos en bruto de diversas fuentes.
- `WeatherService`: Servicio principal que coordina la obtención y procesamiento de datos meteorológicos.
- `WeatherTranslator`: Convierte terminología y medidas meteorológicas entre diferentes sistemas o idiomas.

### ui
Contiene los componentes de interfaz de usuario.

#### forms
Componentes para autenticación de usuarios.

- `LoginActivity`: Maneja la interfaz y lógica de inicio de sesión.
- `RegisterActivity`: Gestiona el registro de nuevos usuarios.

#### outfit
Componentes para visualización y gestión de recomendaciones de vestimenta.

- `OutfitActivity`: Pantalla principal para mostrar recomendaciones de vestuario.
- `OutfitCustomizeAdapter`: Adaptador para personalización de elementos de vestimenta.
- `OutfitViewModel`: Modelo de vista que maneja la lógica de presentación para outfits.
- `OutfitViewModelFactory`: Fábrica para crear instancias de `OutfitViewModel`.

#### outfitcomparison
Componentes para comparar diferentes opciones de vestimenta.

- `OutfitComparisonActivity`: Interfaz para comparar diferentes conjuntos de ropa.
- `OutfitComparisonViewModel`: Modelo de vista para la lógica de comparación.
- `OutfitComparisonViewModelFactory`: Fábrica para crear instancias del modelo anterior.

#### route
Gestión de rutas y su integración con información meteorológica.

- `MapManager`: Gestiona la visualización de mapas y puntos geográficos.
- `RouteManager`: Administra la creación y edición de rutas.
- `RouteWeatherActivity`: Muestra información meteorológica a lo largo de una ruta.
- `WeatherRouteManager`: Integra datos de rutas con información meteorológica.

#### settings
Configuración de la aplicación.

- `SettingsActivity`: Interfaz para modificar ajustes de la aplicación.

#### weather
Visualización de datos meteorológicos.

- `DailyForecastComponent`: Componente UI para mostrar pronósticos diarios.
- `HourlyForecastComponent`: Componente UI para visualizar pronósticos horarios.
- `LocationDialogManager`: Gestiona diálogos para selección de ubicaciones.
- `LocationSuggestionTask`: Proporciona sugerencias de ubicación basadas en entrada del usuario.
- `WeatherActivity`: Pantalla principal para visualizar información meteorológica.
- `WeatherDisplayComponent`: Componente reutilizable para mostrar datos meteorológicos.

### util
Utilidades generales para la aplicación.

- `NavigationManager`: Gestiona la navegación entre diferentes pantallas.
- `OutfitImageMapper`: Mapea condiciones climáticas a imágenes de vestimenta apropiadas.
- `WeatherIconMapper`: Relaciona condiciones meteorológicas con iconos correspondientes.
- `WeatherApplication`: Clase principal de la aplicación que inicializa componentes clave.

---

## Flujo Principal

1. El usuario inicia sesión o se registra a través de las actividades en el paquete `ui.forms`.
2. La interfaz principal (`WeatherActivity`) muestra información meteorológica actual obtenida mediante `WeatherService`.
3. Los usuarios pueden:
   - Ver pronósticos horarios y diarios mediante los componentes especializados.
   - Recibir recomendaciones de vestimenta a través de `OutfitService`.
   - Planificar rutas considerando el clima con `RouteWeatherActivity`.
   - Personalizar sus preferencias usando `SettingsActivity`.

---

## Características Principales

- Pronósticos meteorológicos detallados (actuales, horarios y diarios).
- Recomendaciones de vestimenta basadas en condiciones climáticas.
- Planificación de rutas con integración de información meteorológica.
- Sistema de caché para optimizar el rendimiento y reducir el consumo de datos.
- Personalización de preferencias del usuario.
