package com.example.weatherforecast.service;

import com.example.weatherforecast.model.OutfitRecommendation;
import com.example.weatherforecast.model.CurrentWeather;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OutfitService {

    // Constantes de temperatura
    private static final double VERY_COLD = 5.0;   // <= 5°C
    private static final double COLD = 14.0;       // <= 14°C
    private static final double MILD = 22.0;       // <= 22°C
    private static final double HOT = 28.0;        // <= 28°C
    // > 28°C es MUY CALUROSO
    // Constantes de humedad
    private static final int HIGH_HUMIDITY = 70;    // >= 70% se considera alta humedad
    private static final int LOW_HUMIDITY = 30;     // <= 30% se considera baja humedad

    /**
     * Genera una recomendación de outfit basado en el clima actual y el estilo seleccionado
     */
    public OutfitRecommendation getOutfitRecommendation(CurrentWeather weather, OutfitRecommendation.Style style) {
        double temperature = weather.getTemperature();
        int humidity = weather.getHumidity();
        String condition = weather.getWeatherCondition().toLowerCase();
        boolean isRainy = condition.contains("rain") || condition.contains("shower") || condition.contains("lluvia");
        boolean isSnowy = condition.contains("snow") || condition.contains("nieve");
        boolean isCloudy = condition.contains("cloud") || condition.contains("nublado");
        boolean isWindy = condition.contains("wind") || condition.contains("viento");

        // Ajustar la temperatura según la humedad
        double adjustedTemperature = adjustTemperatureByHumidity(temperature, humidity);

        List<String> tops = new ArrayList<>();
        List<String> bottoms = new ArrayList<>();
        List<String> shoes = new ArrayList<>();
        List<String> outerwear = new ArrayList<>();
        List<String> accessories = new ArrayList<>();

        // Ahora usamos adjustedTemperature en lugar de temperature para las decisiones
        if (adjustedTemperature <= VERY_COLD) {
            generateVeryColdOutfit(style, tops, bottoms, shoes, outerwear, accessories);
        } else if (adjustedTemperature <= COLD) {
            generateColdOutfit(style, tops, bottoms, shoes, outerwear, accessories);
        } else if (adjustedTemperature <= MILD) {
            generateMildOutfit(style, tops, bottoms, shoes, outerwear, accessories);
        } else if (adjustedTemperature <= HOT) {
            generateHotOutfit(style, tops, bottoms, shoes, outerwear, accessories);
        } else {
            generateVeryHotOutfit(style, tops, bottoms, shoes, outerwear, accessories);
        }

        // Añadir recomendaciones específicas de humedad
        if (humidity >= HIGH_HUMIDITY) {
            addHighHumidityItems(adjustedTemperature, accessories);
        }

        // El resto del método permanece igual
        if (isRainy) {
            addRainyDayItems(style, outerwear, accessories, shoes);
        }

        if (isSnowy) {
            addSnowyDayItems(style, outerwear, accessories, shoes);
        }

        if (isWindy) {
            addWindyDayItems(style, accessories);
        }

        return new OutfitRecommendation(tops, bottoms, shoes, outerwear, accessories, style);
    }

    private void generateVeryColdOutfit(OutfitRecommendation.Style style,
                                        List<String> tops, List<String> bottoms,
                                        List<String> shoes, List<String> outerwear,
                                        List<String> accessories) {
        // Accesorios comunes para clima muy frío
        accessories.addAll(Arrays.asList("Gorro de lana", "Bufanda gruesa", "Guantes"));

        switch (style) {
            case CASUAL:
                tops.add("Camiseta térmica de manga larga");
                tops.add("Suéter grueso");
                bottoms.add("Jeans gruesos");
                shoes.add("Botas de invierno");
                outerwear.add("Abrigo de plumas");
                break;

            case SPORTY:
                tops.add("Camiseta térmica deportiva");
                tops.add("Sudadera polar");
                bottoms.add("Pantalón deportivo térmico");
                shoes.add("Zapatillas deportivas resistentes al frío");
                outerwear.add("Chaqueta deportiva aislante");
                break;

            case FORMAL:
                tops.add("Camisa de manga larga");
                tops.add("Chaleco");
                tops.add("Jersey de lana");
                bottoms.add("Pantalón de vestir de lana");
                shoes.add("Zapatos de vestir con calcetines térmicos");
                outerwear.add("Abrigo de lana largo");
                accessories.add("Corbata de lana");
                break;
        }
    }

    private void generateColdOutfit(OutfitRecommendation.Style style,
                                    List<String> tops, List<String> bottoms,
                                    List<String> shoes, List<String> outerwear,
                                    List<String> accessories) {
        accessories.add("Bufanda ligera");

        switch (style) {
            case CASUAL:
                tops.add("Camiseta de manga larga");
                bottoms.add("Jeans");
                shoes.add("Botines");
                outerwear.add("Chaqueta acolchada");
                break;

            case SPORTY:
                tops.add("Camiseta técnica de manga larga");
                bottoms.add("Pantalón deportivo");
                shoes.add("Zapatillas deportivas");
                outerwear.add("Chaqueta cortavientos");
                accessories.add("Gorro deportivo");
                break;

            case FORMAL:
                tops.add("Camisa de vestir");
                tops.add("Jersey fino");
                bottoms.add("Pantalón de vestir");
                shoes.add("Zapatos Oxford");
                outerwear.add("Blazer forrado");
                accessories.add("Pañuelo de bolsillo");
                break;
        }
    }

    private void generateMildOutfit(OutfitRecommendation.Style style,
                                    List<String> tops, List<String> bottoms,
                                    List<String> shoes, List<String> outerwear,
                                    List<String> accessories) {
        switch (style) {
            case CASUAL:
                tops.add("Camiseta de algodón");
                bottoms.add("Jeans o pantalón chino");
                shoes.add("Zapatillas casuales");
                outerwear.add("Sudadera o chaqueta ligera");
                break;

            case SPORTY:
                tops.add("Camiseta técnica");
                bottoms.add("Pantalón corto deportivo o leggings");
                shoes.add("Zapatillas deportivas ligeras");
                outerwear.add("Sudadera con capucha");
                accessories.add("Gorra");
                break;

            case FORMAL:
                tops.add("Camisa de manga larga");
                bottoms.add("Pantalón de vestir ligero");
                shoes.add("Zapatos de vestir");
                outerwear.add("Americana ligera");
                break;
        }
    }

    private void generateHotOutfit(OutfitRecommendation.Style style,
                                   List<String> tops, List<String> bottoms,
                                   List<String> shoes, List<String> outerwear,
                                   List<String> accessories) {
        accessories.add("Gafas de sol");

        switch (style) {
            case CASUAL:
                tops.add("Camiseta de manga corta");
                bottoms.add("Pantalón corto o falda");
                shoes.add("Sandalias o zapatillas ligeras");
                break;

            case SPORTY:
                tops.add("Camiseta técnica transpirable");
                bottoms.add("Pantalón corto deportivo");
                shoes.add("Zapatillas deportivas ligeras");
                accessories.add("Gorra con visera");
                break;

            case FORMAL:
                tops.add("Camisa de manga corta o larga arremangada");
                bottoms.add("Pantalón de vestir ligero");
                shoes.add("Zapatos ligeros");
                break;
        }
    }

    private void generateVeryHotOutfit(OutfitRecommendation.Style style,
                                       List<String> tops, List<String> bottoms,
                                       List<String> shoes, List<String> outerwear,
                                       List<String> accessories) {
        accessories.add("Gafas de sol");
        accessories.add("Sombrero o gorra para protección solar");

        switch (style) {
            case CASUAL:
                tops.add("Camiseta sin mangas o de tejido ligero");
                bottoms.add("Pantalón corto o falda ligera");
                shoes.add("Sandalias");
                break;

            case SPORTY:
                tops.add("Camiseta técnica de manga corta ultraligera");
                bottoms.add("Pantalón corto deportivo ligero");
                shoes.add("Zapatillas deportivas transpirables");
                break;

            case FORMAL:
                tops.add("Camisa de lino o algodón fino");
                bottoms.add("Pantalón ligero de lino o algodón");
                shoes.add("Zapatos ligeros sin calcetines o con calcetines invisibles");
                break;
        }
    }

    private void addRainyDayItems(OutfitRecommendation.Style style,
                                  List<String> outerwear,
                                  List<String> accessories,
                                  List<String> shoes) {
        accessories.add("Paraguas");

        switch (style) {
            case CASUAL:
                outerwear.add("Impermeable o chubasquero");
                if (!shoes.contains("Botas impermeables")) {
                    shoes.clear();
                    shoes.add("Botas impermeables");
                }
                break;

            case SPORTY:
                outerwear.add("Chaqueta impermeable deportiva");
                if (!shoes.contains("Zapatillas impermeables")) {
                    shoes.clear();
                    shoes.add("Zapatillas impermeables");
                }
                break;

            case FORMAL:
                outerwear.add("Gabardina");
                if (!shoes.contains("Zapatos impermeables")) {
                    shoes.clear();
                    shoes.add("Zapatos impermeables");
                }
                break;
        }
    }

    private void addSnowyDayItems(OutfitRecommendation.Style style,
                                  List<String> outerwear,
                                  List<String> accessories,
                                  List<String> shoes) {
        accessories.add("Guantes térmicos");
        accessories.add("Gorro de lana");
        accessories.add("Bufanda gruesa");

        switch (style) {
            case CASUAL:
            case SPORTY:
                if (!shoes.contains("Botas de nieve")) {
                    shoes.clear();
                    shoes.add("Botas de nieve");
                }
                break;

            case FORMAL:
                if (!shoes.contains("Botas de vestir impermeables")) {
                    shoes.clear();
                    shoes.add("Botas de vestir impermeables");
                }
                break;
        }
    }

    private void addWindyDayItems(OutfitRecommendation.Style style, List<String> accessories) {
        if (style == OutfitRecommendation.Style.CASUAL || style == OutfitRecommendation.Style.FORMAL) {
            accessories.add("Bufanda para protección contra el viento");
        } else {
            accessories.add("Bandana o braga de cuello");
        }
    }

    /**
     * Ajusta la temperatura percibida según el nivel de humedad
     * En climas fríos, alta humedad = más frío
     * En climas cálidos, alta humedad = más calor
     */
    private double adjustTemperatureByHumidity(double temperature, int humidity) {
        // Para temperaturas frías (< 15°C), alta humedad hace sentir más frío
        if (temperature < 15.0) {
            if (humidity >= HIGH_HUMIDITY) {
                return temperature - 2.0; // Se siente 2 grados más frío
            } else if (humidity <= LOW_HUMIDITY) {
                return temperature + 1.0; // Se siente 1 grado más cálido
            }
        }
        // Para temperaturas cálidas (> 22°C), alta humedad hace sentir más calor
        else if (temperature > 22.0) {
            if (humidity >= HIGH_HUMIDITY) {
                return temperature + 3.0; // Se siente 3 grados más caluroso
            } else if (humidity <= LOW_HUMIDITY) {
                return temperature - 1.5; // Se siente 1.5 grados más fresco
            }
        }
        return temperature; // Sin ajuste para casos intermedios
    }

    private void addHighHumidityItems(double adjustedTemperature, List<String> accessories) {
        if (adjustedTemperature <= COLD) {
            // Para clima frío y húmedo, añadir capas impermeables
            accessories.add("Ropa interior térmica");
        } else if (adjustedTemperature >= HOT) {
            // Para clima caluroso y húmedo, añadir ropa transpirable
            accessories.add("Ropa de tejidos transpirables");
        }
    }
}