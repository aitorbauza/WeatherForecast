package com.example.weatherforecast.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.ActivityOptionsCompat;

import com.example.weatherforecast.R;
import com.example.weatherforecast.ui.outfit.OutfitActivity;
import com.example.weatherforecast.ui.outfitcomparison.OutfitComparisonActivity;
import com.example.weatherforecast.ui.route.RouteWeatherActivity;
import com.example.weatherforecast.ui.weather.WeatherActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Clase encargada de gestionar la navegación del bottomnav
 */
public class NavigationManager {
    private final Context context;
    private final BottomNavigationView bottomNavigation;
    private String currentCity;
    private final ActivityType currentActivity;
    private static final String EXTRA_FORCE_RELOAD = "FORCE_RELOAD_WEATHER";
    private static final String EXTRA_PREVIOUS_ACTIVITY = "PREVIOUS_ACTIVITY";  // Registrar la actividad anterior

    public enum ActivityType {
        WEATHER,
        OUTFIT,
        ROUTE,
        OUTFIT_COMPARISON
    }

    public NavigationManager(Context context, BottomNavigationView bottomNavigation,
                             String currentCity, ActivityType currentActivity) {
        this.context = context;
        this.bottomNavigation = bottomNavigation;
        this.currentCity = currentCity;
        this.currentActivity = currentActivity;
    }

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
            case OUTFIT_COMPARISON:
                bottomNavigation.setSelectedItemId(R.id.nav_outfit_comparison);
                break;
        }

        // Listener para gestionar la navegación
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // No navegar si ya estamos en la actividad seleccionada
            if ((itemId == R.id.nav_weather && currentActivity == ActivityType.WEATHER) ||
                    (itemId == R.id.nav_clothing && currentActivity == ActivityType.OUTFIT) ||
                    (itemId == R.id.nav_route && currentActivity == ActivityType.ROUTE) ||
                    (itemId == R.id.nav_outfit_comparison && currentActivity == ActivityType.OUTFIT_COMPARISON)) {
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
            } else if (itemId == R.id.nav_outfit_comparison) {
                navigateToOutfitComparisonScreen();
                return true;
            }
            return false;
        });
    }

    // Método que te lleva a la pantalla de clima
    private void navigateToWeatherScreen() {
        Intent intent = new Intent(context, WeatherActivity.class);
        intent.putExtra("CITY_NAME", currentCity);

        // Forzar la recarga de datos solo si venimos de una actividad diferente
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.getIntent().hasExtra("username")) {
                String username = activity.getIntent().getStringExtra("username");
                intent.putExtra("username", username);
            }
        }
        intent.putExtra(EXTRA_PREVIOUS_ACTIVITY, currentActivity.name());

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Este flag asegura que solo se abra una instancia de WeatherActivity

        context.startActivity(intent);
        applyTransition();
    }

    // Método que te lleva a la pantalla de outfit
    private void navigateToOutfitScreen() {
        Intent intent = new Intent(context, OutfitActivity.class);
        intent.putExtra("CITY_NAME", currentCity);

        // Solo forzar si venimos de otra actividad y necesitamos nueva info del clima
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.getIntent().hasExtra("username")) {
                String username = activity.getIntent().getStringExtra("username");
                intent.putExtra("username", username);
            }
        }
        intent.putExtra(EXTRA_PREVIOUS_ACTIVITY, currentActivity.name());

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Este flag asegura que solo se abra una instancia de OutfitActivity
        context.startActivity(intent);
        applyTransition();
    }

    // Método que te lleva a la pantalla de ruta
    private void navigateToRouteWeatherScreen() {
        Intent intent = new Intent(context, RouteWeatherActivity.class);
        intent.putExtra("ORIGIN_CITY", currentCity);
        intent.putExtra("LAZY_LOAD", true);

        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.getIntent().hasExtra("username")) {
                String username = activity.getIntent().getStringExtra("username");
                intent.putExtra("username", username);
            }
        }
        // Añadir información de la actividad anterior para ayudar en la depuración
        intent.putExtra(EXTRA_PREVIOUS_ACTIVITY, currentActivity.name());

        // Bundle que contiene las opciones de animación
        Bundle options = ActivityOptionsCompat.makeCustomAnimation(
                context,
                android.R.anim.fade_in,
                android.R.anim.fade_out).toBundle();

        // Usar SINGLE_TOP para evitar múltiples instancias de RouteWeatherActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // Estos flags asegura que solo se abra una instancia de RouteWeatherActivity

        context.startActivity(intent, options);
        applyTransition();
    }

    public void navigateToOutfitComparisonScreen() {
        Intent intent = new Intent(context, OutfitComparisonActivity.class);
        intent.putExtra("CITY_NAME", currentCity);

        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.getIntent().hasExtra("username")) {
                String username = activity.getIntent().getStringExtra("username");
                intent.putExtra("username", username);
            }
        }
        intent.putExtra(EXTRA_PREVIOUS_ACTIVITY, currentActivity.name());

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        applyTransition();
    }

    // Método para aplicar la transición
    private void applyTransition() {
        // Si esta navegación ocurre en una actividad, y no en un fragmento o servicio
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out);
        }
    }

    // Método para verificar si se debe forzar la recarga de datos
    public static boolean shouldForceReload(Intent intent) {
        return intent != null && intent.getBooleanExtra(EXTRA_FORCE_RELOAD, false);
    }

    public void updateCurrentCity(String newCity) {
        if (newCity != null && !newCity.isEmpty()) {
            this.currentCity = newCity;
        }
    }

}