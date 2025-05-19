package com.example.weatherforecast.ui;

import android.os.AsyncTask;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class LocationSuggestionTask extends AsyncTask<String, Void, List<String>> {
    private static final String GEOCODING_API_URL = "https://api.openweathermap.org/geo/1.0/direct";
    private static final String API_KEY = "fd55aeb51961a4033188497fa3b1f146"; // OpenWeather API
    private static final int LIMIT = 5; // Número de sugerencias a mostrar

    private final ArrayAdapter<String> adapter;

    public LocationSuggestionTask(ArrayAdapter<String> adapter) {
        this.adapter = adapter;
    }

    @Override
    protected List<String> doInBackground(String... params) {
        List<String> suggestions = new ArrayList<>();

        try {
            String query = URLEncoder.encode(params[0], "UTF-8");
            String urlString = GEOCODING_API_URL +
                    "?q=" + query +
                    "&limit=" + LIMIT +
                    "&appid=" + API_KEY;

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONArray jsonArray = new JSONArray(response.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject location = jsonArray.getJSONObject(i);
                String name = location.getString("name");
                String country = location.getString("country");

                // Añadir el nombre de estado/provincia si está disponible (principalmente para USA)
                String state = "";
                if (location.has("state")) {
                    state = location.getString("state");
                    suggestions.add(name + ", " + state + ", " + country);
                } else {
                    suggestions.add(name + ", " + country);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return suggestions;
    }

    @Override
    protected void onPostExecute(List<String> suggestions) {
        adapter.clear();
        adapter.addAll(suggestions);
        adapter.notifyDataSetChanged();
    }
}
