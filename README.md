# 🌦️ WEARTHER - AITOR BAUZÁ GÓMEZ

> Aplicación de pronóstico del tiempo y recomendaciones de vestimenta

---

## 📝 Descripción General

**WEARTHER** proporciona:

- Pronósticos meteorológicos (actuales, horarios y diarios)
- Recomendaciones de vestimenta según el clima
- Planificación de rutas con condiciones meteorológicas
- Interfaz intuitiva con personalización de preferencias

---

## 🗂️ Estructura del Proyecto

**Paquete raíz:** `com.example.weatherforecast`

### 📁 controller

Coordina solicitudes del usuario entre la UI y los servicios.

- **`WeatherController`**  
  Gestiona peticiones de clima, procesa entradas del usuario y entrega respuestas.

---

### 📁 data

Acceso y persistencia de datos.

- **`DBHelper`**  
  Conexión y operaciones con la base de datos.

- **`WeatherCache`**  
  Caché temporal para reducir llamadas a APIs externas.

---

### 📁 dto (Data Transfer Objects)

Objetos para transferir datos entre capas del sistema.

- **`ForecastResponse`**  
  Encapsula datos de pronóstico.

- **`WeatherResponse`**  
  Formatea la respuesta del clima actual.

---

### 📁 model

Define las entidades del dominio.

- **`CurrentWeather`** — Clima actual para una ubicación  
- **`DailyForecast`** — Pronóstico diario  
- **`HourlyForecast`** — Pronóstico por hora  
- **`OutfitRecommendation`** — Recomendaciones de vestimenta  
- **`RoutePoint`** — Punto geográfico en una ruta  
- **`SavedOutfitEntry`** — Outfits guardados por el usuario  
- **`UserPreferences`** — Preferencias del usuario

---

### 📁 repository

Abstrae la lógica de acceso a datos.

- **`PreferencesRepository`** — CRUD para preferencias  
- **`WeatherRepository`** — Manejo de datos meteorológicos desde caché o API

---

### 📁 service

Contiene la lógica de negocio.

- **`ForecastProcessor`** — Procesa datos de pronóstico  
- **`OutfitService`** — Genera recomendaciones de vestimenta  
- **`WeatherService`** — Servicio principal de clima  
- **`WeatherTranslator`** — Conversión de unidades e idiomas  
- **`WeatherDataProcessor`**, **`OutfitDisplayHelper`**

---

### 📁 ui

Componentes de interfaz de usuario.

#### 🔐 forms (Autenticación)

- **`LoginActivity`** — Inicio de sesión  
- **`RegisterActivity`** — Registro de usuario

#### 👚 outfit (Vestimenta)

- **`OutfitActivity`** — Pantalla principal de recomendaciones  
- **`OutfitCustomizeAdapter`** — Personalización de outfits  
- **`OutfitViewModel`**, **`OutfitViewModelFactory`**

#### 🔄 outfitcomparison (Comparación)

- **`OutfitComparisonActivity`** — Comparación de conjuntos  
- **`OutfitComparisonViewModel`**, **`Factory`**

#### 🗺️ route (Rutas)

- **`MapManager`** — Visualización de mapas  
- **`RouteManager`** — Creación y edición de rutas  
- **`RouteWeatherActivity`** — Clima a lo largo de la ruta  
- **`WeatherRouteManager`**

#### ⚙️ settings (Configuraciones)

- **`SettingsActivity`** — Ajustes de la app

#### 🌤️ weather (Visualización del clima)

- **`DailyForecastComponent`**, **`HourlyForecastComponent`**  
- **`LocationDialogManager`**, **`LocationSuggestionTask`**  
- **`WeatherActivity`**, **`WeatherDisplayComponent`**

---

### 🛠️ util

Utilidades generales.

- **`NavigationManager`** — Navegación entre pantallas  
- **`OutfitImageMapper`** — Mapea clima a vestimenta  
- **`WeatherIconMapper`** — Íconos meteorológicos  
- **`WeatherApplication`** — Inicializa la app

---

## 🔄 Flujo Principal del Usuario

1. Inicia sesión o se registra (`LoginActivity`, `RegisterActivity`)
2. Consulta el clima actual (`WeatherActivity`)
3. Puede:
   - Ver pronósticos diarios y horarios
   - Recibir recomendaciones de ropa (`OutfitService`)
   - Planificar rutas con clima (`RouteWeatherActivity`)
   - Configurar sus preferencias (`SettingsActivity`)

---

## 🌟 Características Principales

- ✅ Pronósticos meteorológicos: actuales, por hora, diarios  
- 👕 Recomendaciones de vestimenta basadas en el clima  
- 🧭 Planificación de rutas con datos meteorológicos  
- 🧠 Sistema de caché para eficiencia y bajo consumo de datos  
- ⚙️ Personalización de preferencias del usuario

---

