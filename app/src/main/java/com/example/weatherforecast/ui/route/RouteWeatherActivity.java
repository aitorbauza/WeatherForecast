package com.example.weatherforecast.ui.route;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherforecast.R;
import com.example.weatherforecast.util.NavigationManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.weatherforecast.util.NavigationManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Actividad que muestra información meteorológica detallada
 * entre un origen y un destino dentro de España.
 */
public class RouteWeatherActivity extends AppCompatActivity {

    // APIs keys
    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyBF5Bi-0WWKdREJqpJwdFMOl1bA5y7Xbv8";
    private static final String WEATHER_API_KEY = "fd55aeb51961a4033188497fa3b1f146";

    // Coordenadas que definen el área de España
    private static final double SPAIN_NORTH = 43.8;
    private static final double SPAIN_SOUTH = 36.0;
    private static final double SPAIN_EAST = 3.3;
    private static final double SPAIN_WEST = -9.3;

    // Número de puntos intermedios para consultar el clima
    private static final int INTERMEDIATE_POINTS = 3;
    private static final String DEFAULT_CITY = "Palma de Mallorca";

    private TextInputEditText originEditText;
    private TextInputEditText destinationEditText;
    private MaterialButton searchRouteButton;
    private TextView weatherInfoTextView;

    // HTTP Request
    private RequestQueue requestQueue;

    private RoutePoint originPoint;
    private RoutePoint destinationPoint;
    private List<RoutePoint> intermediatePoints = new ArrayList<>();
    private List<RoutePoint> allPoints = new ArrayList<>();

    private BottomNavigationView bottomNavigation;
    private NavigationManager navigationManager;
    private String currentCity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_weather);

        // Inicializar Volley para peticiones HTTP
        // Volley es una biblioteca para hacer solicitudes HTTP en Android
        requestQueue = Volley.newRequestQueue(this);

        originEditText = findViewById(R.id.originEditText);
        destinationEditText = findViewById(R.id.destinationEditText);
        searchRouteButton = findViewById(R.id.searchRouteButton);
        weatherInfoTextView = findViewById(R.id.weatherInfoTextView);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        if (getIntent().hasExtra("CITY_NAME")) {
            currentCity = getIntent().getStringExtra("CITY_NAME");
        } else {
            currentCity = DEFAULT_CITY; // Ciudad por defecto si no se especifica
        }

        weatherInfoTextView.setText("Introduce origen y destino para ver información meteorológica detallada.");

        searchRouteButton.setOnClickListener(this::searchRoute);
        navigationManager = new NavigationManager(
                this,
                bottomNavigation,
                currentCity, // No se usa en esta actividad
                NavigationManager.ActivityType.ROUTE
        );

        navigationManager.setupBottomNavigation();
        bottomNavigation.setSelectedItemId(R.id.nav_route);
    }

    //Busca la ruta entre el origen y destino especificados
    private void searchRoute(View view) {
        String origin = originEditText.getText() != null ? originEditText.getText().toString().trim() : "";
        String destination = destinationEditText.getText() != null ? destinationEditText.getText().toString().trim() : "";

        if (origin.isEmpty() || destination.isEmpty()) {
            Toast.makeText(this, "Por favor, introduce origen y destino", Toast.LENGTH_SHORT).show();
            return;
        }

        // Limpiar datos anteriores
        originPoint = null;
        destinationPoint = null;
        intermediatePoints.clear();
        allPoints.clear();

        // Mostrar indicador de carga
        searchRouteButton.setEnabled(false);
        searchRouteButton.setText("Buscando...");
        weatherInfoTextView.setText("Buscando información meteorológica...");

        // Realizar la geocodificación de las ciudades
        geocodeLocation(origin, true);
        geocodeLocation(destination, false);
    }


    //Geocodifica una ubicación para obtener sus coordenadas, restringiendo a España
    private void geocodeLocation(String location, boolean isOrigin) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json" +
                "?address=" + location +
                "&components=country:ES" + // Restricción a España
                "&key=" + GOOGLE_MAPS_API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        if (results.length() > 0) {
                            JSONObject result = results.getJSONObject(0);
                            JSONObject locationObj = result.getJSONObject("geometry").getJSONObject("location");

                            double lat = locationObj.getDouble("lat");
                            double lng = locationObj.getDouble("lng");

                            // Verificar si está dentro de España
                            if (lat > SPAIN_SOUTH && lat < SPAIN_NORTH && lng > SPAIN_WEST && lng < SPAIN_EAST) {
                                String name = location;
                                for (int i = 0; i < result.getJSONArray("address_components").length(); i++) {
                                    JSONObject component = result.getJSONArray("address_components").getJSONObject(i);
                                    JSONArray types = component.getJSONArray("types");
                                    for (int j = 0; j < types.length(); j++) {
                                        if (types.getString(j).equals("locality")) {
                                            name = component.getString("long_name");
                                            break;
                                        }
                                    }
                                }

                                RoutePoint point = new RoutePoint(name, lat, lng);
                                if (isOrigin) {
                                    originPoint = point;
                                } else {
                                    destinationPoint = point;
                                }

                                // Si tenemos origen y destino, buscamos la ruta
                                if (originPoint != null && destinationPoint != null) {
                                    fetchRoute();
                                }
                            } else {
                                handleLocationError("La ubicación debe estar dentro de España: " + location);
                            }
                        } else {
                            handleLocationError("No se encontró la ubicación en España: " + location);
                        }
                    } catch (JSONException e) {
                        handleLocationError("Error al procesar la ubicación: " + e.getMessage());
                    }
                },
                error -> handleLocationError("Error de red: " + error.getMessage())
        );

        requestQueue.add(request);
    }

    //Busca la ruta entre el origen y destino usando la API de Google Directions
    private void fetchRoute() {
        if (originPoint == null || destinationPoint == null) return;

        String url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" + originPoint.lat + "," + originPoint.lng +
                "&destination=" + destinationPoint.lat + "," + destinationPoint.lng +
                "&key=" + GOOGLE_MAPS_API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray routes = response.getJSONArray("routes");
                        if (routes.length() > 0) {
                            // Extraer los puntos de la ruta
                            JSONObject route = routes.getJSONObject(0);

                            // Obtener la distancia y duración total
                            JSONArray legs = route.getJSONArray("legs");
                            JSONObject leg = legs.getJSONObject(0);
                            String distance = leg.getJSONObject("distance").getString("text");
                            String duration = leg.getJSONObject("duration").getString("text");

                            // Decodificar polyline para obtener puntos intermedios
                            String encodedPolyline = route.getJSONObject("overview_polyline").getString("points");
                            List<LatLng> path = decodePolyline(encodedPolyline);

                            // Generar puntos intermedios equidistantes
                            generateIntermediatePoints(path);

                            // Preparar información de la ruta
                            weatherInfoTextView.setText("Ruta de " + originPoint.name + " a " +
                                    destinationPoint.name + "\n" +
                                    "Distancia: " + distance + " | Duración: " + duration +
                                    "\n\nObteniendo información del clima...");

                            // Obtener clima para todos los puntos
                            fetchWeatherForAllPoints();
                        } else {
                            handleLocationError("No se encontró una ruta entre las ubicaciones");
                        }
                    } catch (JSONException e) {
                        handleLocationError("Error al procesar la ruta: " + e.getMessage());
                    }
                },
                error -> handleLocationError("Error de red: " + error.getMessage())
        );

        requestQueue.add(request);
    }

    //Decodifica una polyline codificada de Google Maps
    // PolyLine es una forma de representar una ruta en un mapa
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(p);
        }

        return poly;
    }

    //Genera puntos intermedios para consultar el clima
    private void generateIntermediatePoints(List<LatLng> path) {
        if (path.size() < 2) return;

        intermediatePoints.clear();

        // Seleccionar puntos a intervalos regulares
        int step = path.size() / (INTERMEDIATE_POINTS + 1);
        if (step < 1) step = 1;

        for (int i = 1; i <= INTERMEDIATE_POINTS; i++) {
            int index = i * step;
            if (index < path.size()) {
                LatLng point = path.get(index);
                String name = "Punto " + i;

                // Crear punto intermedio y añadirlo a la lista
                RoutePoint intermediatePoint = new RoutePoint(name, point.lat, point.lng);
                intermediatePoints.add(intermediatePoint);

                // Intentar obtener el nombre real de la localidad para este punto
                getLocationName(intermediatePoint);
            }
        }

        // Preparar lista con todos los puntos (origen, intermedios, destino)
        allPoints.clear();
        allPoints.add(originPoint);
        allPoints.addAll(intermediatePoints);
        allPoints.add(destinationPoint);
    }

    // Intenta obtener el nombre de una localidad para un punto intermedio
    private void getLocationName(RoutePoint point) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json" +
                "?latlng=" + point.lat + "," + point.lng +
                "&key=" + GOOGLE_MAPS_API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        if (results.length() > 0) {
                            // Buscar componente de tipo localidad
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject result = results.getJSONObject(i);
                                JSONArray components = result.getJSONArray("address_components");

                                for (int j = 0; j < components.length(); j++) {
                                    JSONObject component = components.getJSONObject(j);
                                    JSONArray types = component.getJSONArray("types");

                                    for (int k = 0; k < types.length(); k++) {
                                        if (types.getString(k).equals("locality")) {
                                            point.name = component.getString("long_name");
                                            updateWeatherInfo();
                                            return;
                                        } else if (types.getString(k).equals("administrative_area_level_3")) {
                                            point.name = component.getString("long_name");
                                        }
                                    }
                                }
                            }

                            // Si no encontramos localidad, usar formatted_address que sirve como fallback
                            String formattedAddress = results.getJSONObject(0).getString("formatted_address");
                            String[] addressParts = formattedAddress.split(",");
                            if (addressParts.length > 0) {
                                point.name = addressParts[0].trim();
                                updateWeatherInfo();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> { } // No ocurre nada
        );

        requestQueue.add(request);
    }

    // Obtiene información meteorológica para todos los puntos
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private int weatherRequestsCompleted = 0;

    private void fetchWeatherForAllPoints() {
        weatherRequestsCompleted = 0;

        for (RoutePoint point : allPoints) {
            fetchWeatherForPoint(point);
        }
    }

    // Obtiene el clima para un punto específico
    private void fetchWeatherForPoint(RoutePoint point) {
        if (point == null) return;

        executor.execute(() -> {
            String url = "https://api.openweathermap.org/data/2.5/weather" +
                    "?lat=" + point.lat +
                    "&lon=" + point.lng +
                    "&units=metric" +
                    "&appid=" + WEATHER_API_KEY;

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            double temp = response.getJSONObject("main").getDouble("temp");
                            double humidity = response.getJSONObject("main").getDouble("humidity");
                            double windSpeed = response.getJSONObject("wind").getDouble("speed");
                            String weatherDesc = response.getJSONArray("weather").getJSONObject(0).getString("main");
                            String weatherDetail = response.getJSONArray("weather").getJSONObject(0).getString("description");

                            point.temperature = (int) Math.round(temp);
                            point.weatherDescription = weatherDesc;
                            point.humidity = (int) Math.round(humidity);
                            point.windSpeed = windSpeed;
                            point.weatherDetail = weatherDetail;

                            runOnUiThread(() -> {
                                weatherRequestsCompleted++;

                                // Actualizar interfaz con información recibida
                                updateWeatherInfo();

                                // Si hemos recibido información de todos los puntos, habilitar botón de búsqueda
                                if (weatherRequestsCompleted >= allPoints.size()) {
                                    searchRouteButton.setEnabled(true);
                                    searchRouteButton.setText("Buscar Ruta");
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> runOnUiThread(() -> Toast.makeText(RouteWeatherActivity.this,
                            "Error al obtener el clima de " + point.name,
                            Toast.LENGTH_SHORT).show())
            );

            requestQueue.add(request);
        });
    }

    //Actualiza la información del clima en la UI
    private void updateWeatherInfo() {
        if (allPoints.isEmpty()) return;

        StringBuilder infoBuilder = new StringBuilder();

        try {
            // Obtener información de la ruta entre origen y destino
            String distanceInfo = "Ruta: " + originPoint.name + " → " + destinationPoint.name;
            infoBuilder.append(distanceInfo).append("\n\n");

            // Información detallada del clima para cada punto
            for (int i = 0; i < allPoints.size(); i++) {
                RoutePoint point = allPoints.get(i);

                // Si no tenemos datos meteorológicos, mostrar que estamos cargando
                if (point.weatherDescription == null) {
                    infoBuilder.append(point.name).append(": Cargando...\n");
                    continue;
                }

                // Información básica con emoji
                String emoji = getWeatherEmoji(point.weatherDescription);
                String basicInfo = point.name + ": " + point.temperature + "°C " + emoji;
                infoBuilder.append(basicInfo).append("\n");

                // Datos detallados
                String weatherInfo = "   - " + getWeatherDescriptionInSpanish(point.weatherDetail);
                String detailInfo = "   - Humedad: " + point.humidity + "% | Viento: " +
                        new DecimalFormat("#.#").format(point.windSpeed) + " m/s";

                infoBuilder.append(weatherInfo).append("\n");
                infoBuilder.append(detailInfo).append("\n");

                // Añadir flecha direccional excepto para el último punto
                if (i < allPoints.size() - 1) {
                    infoBuilder.append("   ↓\n");
                }
            }

            // Información sobre condiciones generales y recomendaciones
            infoBuilder.append("\nResumen de la ruta:\n");
            addRouteSummary(infoBuilder);

            // Actualizar TextView
            weatherInfoTextView.setText(infoBuilder.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Añade un resumen de las condiciones generales de la ruta
    // infobuilder contiene la información que se va a mostrar
    private void addRouteSummary(StringBuilder infoBuilder) {
        // Calcular temperatura media
        double tempSum = 0;
        int tempCount = 0;

        // Contar diferentes condiciones climáticas
        int rainCount = 0;
        int cloudyCount = 0;
        int clearCount = 0;

        // Velocidad máxima del viento
        double maxWind = 0;
        String maxWindLocation = "";

        // Recorrer todos los puntos
        for (RoutePoint point : allPoints) {
            if (point.temperature != null) {
                tempSum += point.temperature;
                tempCount++;

                // Contar condiciones
                if (point.weatherDescription != null) {
                    if (point.weatherDescription.contains("Rain") ||
                            point.weatherDescription.contains("Drizzle") ||
                            point.weatherDescription.contains("Thunderstorm")) {
                        rainCount++;
                    } else if (point.weatherDescription.contains("Cloud")) {
                        cloudyCount++;
                    } else if (point.weatherDescription.contains("Clear")) {
                        clearCount++;
                    }
                }

                // Verificar viento máximo
                if (point.windSpeed > maxWind) {
                    maxWind = point.windSpeed;
                    maxWindLocation = point.name;
                }
            }
        }

        // Añadir temperatura media
        if (tempCount > 0) {
            double avgTemp = tempSum / tempCount;
            infoBuilder.append("• Temperatura media: ").append(new DecimalFormat("#.#").format(avgTemp)).append("°C\n");
        }

        // Añadir condición predominante
        if (rainCount > cloudyCount && rainCount > clearCount) {
            infoBuilder.append("• Condición predominante: Lluvia o tormenta\n");
        } else if (cloudyCount > rainCount && cloudyCount > clearCount) {
            infoBuilder.append("• Condición predominante: Nubosidad\n");
        } else if (clearCount > rainCount && clearCount > cloudyCount) {
            infoBuilder.append("• Condición predominante: Despejado\n");
        } else {
            infoBuilder.append("• Condiciones variables a lo largo de la ruta\n");
        }

        // Añadir información de viento si es relevante
        if (maxWind > 5.5) {  // Viento moderado según escala Beaufort
            infoBuilder.append("• Viento fuerte en ").append(maxWindLocation)
                    .append(" (").append(new DecimalFormat("#.#").format(maxWind)).append(" m/s)\n");
        }
    }

    private String getWeatherEmoji(String description) {
        if (description == null) return "";
        if (description.contains("Clear")) return "☀️";
        if (description.contains("Cloud")) return "☁️";
        if (description.contains("Rain")) return "🌧️";
        if (description.contains("Snow")) return "❄️";
        if (description.contains("Thunderstorm")) return "⛈️";
        if (description.contains("Drizzle")) return "🌦️";
        if (description.contains("Fog") || description.contains("Mist")) return "🌫️";
        return "☁️";
    }

    // Convierte la descripción del clima al español
    private String getWeatherDescriptionInSpanish(String weatherDesc) {
        if (weatherDesc == null) return "";

        // Descripciones principales
        if (weatherDesc.contains("clear sky")) return "Cielo despejado";
        if (weatherDesc.contains("few clouds")) return "Pocas nubes";
        if (weatherDesc.contains("scattered clouds")) return "Nubes dispersas";
        if (weatherDesc.contains("broken clouds")) return "Nubosidad variable";
        if (weatherDesc.contains("overcast clouds")) return "Cielo cubierto";
        if (weatherDesc.contains("light rain")) return "Lluvia ligera";
        if (weatherDesc.contains("moderate rain")) return "Lluvia moderada";
        if (weatherDesc.contains("heavy intensity rain")) return "Lluvia intensa";
        if (weatherDesc.contains("light snow")) return "Nieve ligera";
        if (weatherDesc.contains("snow")) return "Nieve";
        if (weatherDesc.contains("thunderstorm")) return "Tormenta eléctrica";
        if (weatherDesc.contains("drizzle")) return "Llovizna";
        if (weatherDesc.contains("mist")) return "Niebla ligera";
        if (weatherDesc.contains("fog")) return "Niebla";

        // Si no hay coincidencia exacta
        switch (weatherDesc) {
            case "Clear":
                return "Soleado";
            case "Clouds":
                return "Nuboso";
            case "Rain":
                return "Lluvia";
            case "Snow":
                return "Nieve";
            case "Thunderstorm":
                return "Tormenta";
            case "Drizzle":
                return "Llovizna";
            case "Mist":
            case "Fog":
                return "Niebla";
            default:
                return weatherDesc;
        }
    }

    // Maneja errores de localización o ruta
    private void handleLocationError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        searchRouteButton.setEnabled(true);
        searchRouteButton.setText("Buscar Ruta");
    }

    // Clase interna para representar un punto simple de latitud/longitud
    private static class LatLng {
        double lat;
        double lng;

        LatLng(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }

    // Clase interna para representar un punto en la ruta con información meteorológica
    private static class RoutePoint {
        String name;
        double lat;
        double lng;
        Integer temperature;
        String weatherDescription;
        String weatherDetail;
        Integer humidity;
        Double windSpeed;

        RoutePoint(String name, double lat, double lng) {
            this.name = name;
            this.lat = lat;
            this.lng = lng;
        }
    }
}