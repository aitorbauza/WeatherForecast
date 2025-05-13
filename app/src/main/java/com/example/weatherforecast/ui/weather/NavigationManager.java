package com.example.weatherforecast.ui.weather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.app.ActivityOptionsCompat;

import com.example.weatherforecast.R;
import com.example.weatherforecast.ui.OutfitActivity;
import com.example.weatherforecast.ui.route.RouteWeatherActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationManager {
    private final Context context;
    private final BottomNavigationView bottomNavigation;
    private final String currentCity;

    public NavigationManager(Context context, BottomNavigationView bottomNavigation, String currentCity) {
        this.context = context;
        this.bottomNavigation = bottomNavigation;
        this.currentCity = currentCity;
    }

    public void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_weather);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_weather) {
                // Already in weather activity
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

    private void navigateToOutfitScreen() {
        Intent intent = new Intent(context, OutfitActivity.class);
        intent.putExtra("CITY_NAME", currentCity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    private void navigateToRouteWeatherScreen() {
        new Handler().postDelayed(() -> {

                    Intent intent = new Intent(context, RouteWeatherActivity.class);
                    intent.putExtra("ORIGIN_CITY", currentCity);

                    // Usa un bundle para actividades recientes
                    Bundle options = ActivityOptionsCompat.makeCustomAnimation(
                            context,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out).toBundle();

                    context.startActivity(intent, options);
                }, 100);

        // Si esta navegación ocurre en una actividad, y no en un fragmento o servicio
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out);
        }
    }
}
