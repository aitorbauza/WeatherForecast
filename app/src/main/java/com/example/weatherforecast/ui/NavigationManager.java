package com.example.weatherforecast.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.ActivityOptionsCompat;

import com.example.weatherforecast.R;
import com.example.weatherforecast.ui.route.RouteWeatherActivity;
import com.example.weatherforecast.ui.weather.WeatherActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationManager {
    private final Context context;
    private final BottomNavigationView bottomNavigation;
    private String currentCity;
    private final ActivityType currentActivity;
    private static final String EXTRA_FORCE_RELOAD = "FORCE_RELOAD_WEATHER";
    private static final String EXTRA_PREVIOUS_ACTIVITY = "PREVIOUS_ACTIVITY";  // Nuevo extra para rastrear la actividad anterior

    /**
     * Define los tipos de actividades que pueden usar esta clase
     */
    public enum ActivityType {
        WEATHER,
        OUTFIT,
        ROUTE
    }

    /**
     * Constructor para la clase NavigationManager
     *
     * @param context El contexto de la actividad
     * @param bottomNavigation La vista de navegación inferior
     * @param currentCity El nombre de la ciudad actual
     * @param currentActivity El tipo de actividad actual
     */
    public NavigationManager(Context context, BottomNavigationView bottomNavigation,
                             String currentCity, ActivityType currentActivity) {
        this.context = context;
        this.bottomNavigation = bottomNavigation;
        this.currentCity = currentCity;
        this.currentActivity = currentActivity;
    }

    /**
     * Configura la navegación inferior para la actividad actual
     */
    public void setupBottomNavigation() {
        // Establece el ítem seleccionado según la actividad actual
        switch (currentActivity) {
            case WEATHER:
                bottomNavigation.setSelectedItemId(R.id.nav_weather);
                break;
            case OUTFIT:
                bottomNavigation.setSelectedItemId(R.id.nav_clothing);
                break;
            case ROUTE:
                bottomNavigation.setSelectedItemId(R.id.nav_route);
                break;
        }

        // Configura el listener para gestionar la navegación
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // No navegar si ya estamos en la actividad seleccionada
            if ((itemId == R.id.nav_weather && currentActivity == ActivityType.WEATHER) ||
                    (itemId == R.id.nav_clothing && currentActivity == ActivityType.OUTFIT) ||
                    (itemId == R.id.nav_route && currentActivity == ActivityType.ROUTE)) {
                return true;
            }

            // Navegar a la actividad seleccionada
            if (itemId == R.id.nav_weather) {
                navigateToWeatherScreen();
                return true;
            } else if (itemId == R.id.nav_clothing) {
                navigateToOutfitScreen();
                return true;
            } else if (itemId == R.id.nav_route) {
                navigateToRouteWeatherScreen();
                return true;
            }
            return false;
        });
    }

    private void navigateToWeatherScreen() {
        Intent intent = new Intent(context, WeatherActivity.class);
        intent.putExtra("CITY_NAME", currentCity);

        // Añadir información de la actividad anterior para ayudar en la depuración
        intent.putExtra(EXTRA_PREVIOUS_ACTIVITY, currentActivity.name());

        // Forzar la recarga de datos solo si venimos de una actividad diferente
        if (currentActivity != ActivityType.WEATHER) {
            intent.putExtra(EXTRA_FORCE_RELOAD, true);
        }

        // Usar una estrategia de flags uniforme para todas las actividades
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(intent);
        applyTransition();
    }

    /**
     * Navega a la pantalla de vestimenta
     */
    // En NavigationManager.java - modificar navigateToOutfitScreen()
    private void navigateToOutfitScreen() {
        Intent intent = new Intent(context, OutfitActivity.class);
        intent.putExtra("CITY_NAME", currentCity);
        intent.putExtra(EXTRA_PREVIOUS_ACTIVITY, currentActivity.name());

        // No forzar la recarga para preservar los datos del outfit
        // Solo forzar si venimos de otra actividad y necesitamos nueva info del clima
        if (currentActivity != ActivityType.OUTFIT) {
            intent.putExtra(EXTRA_FORCE_RELOAD, false);  // Cambiar a false para no perder datos
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        applyTransition();
    }

    /**
     * Navega a la pantalla de ruta meteorológica
     * Versión optimizada que usa lazy loading
     */
    private void navigateToRouteWeatherScreen() {
        Intent intent = new Intent(context, RouteWeatherActivity.class);
        intent.putExtra("ORIGIN_CITY", currentCity);
        intent.putExtra("LAZY_LOAD", true);  // Añadimos un flag para lazy loading

        // Añadir información de la actividad anterior para ayudar en la depuración
        intent.putExtra(EXTRA_PREVIOUS_ACTIVITY, currentActivity.name());

        // Usa un bundle para actividades recientes
        Bundle options = ActivityOptionsCompat.makeCustomAnimation(
                context,
                android.R.anim.fade_in,
                android.R.anim.fade_out).toBundle();

        // Usar SINGLE_TOP para evitar múltiples instancias de RouteWeatherActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        context.startActivity(intent, options);
        applyTransition();
    }

    /**
     * Aplica la transición de animación
     */
    private void applyTransition() {
        // Si esta navegación ocurre en una actividad, y no en un fragmento o servicio
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out);
        }
    }

    /**
     * Verifica si se debe forzar la recarga de datos
     * @return true si se debe forzar la recarga
     */
    public static boolean shouldForceReload(Intent intent) {
        return intent != null && intent.getBooleanExtra(EXTRA_FORCE_RELOAD, false);
    }

    /**
     * Obtiene la actividad anterior de la que se navegó
     * @return Nombre de la actividad anterior o null si no está disponible
     */
    public static String getPreviousActivity(Intent intent) {
        return intent != null ? intent.getStringExtra(EXTRA_PREVIOUS_ACTIVITY) : null;
    }

    public void updateCurrentCity(String newCity) {
        if (newCity != null && !newCity.isEmpty()) {
            this.currentCity = newCity;
        }
    }
}