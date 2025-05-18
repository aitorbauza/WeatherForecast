package com.example.weatherforecast.service;

import com.example.weatherforecast.model.OutfitRecommendation;
import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.model.UserPreferences;

import java.util.ArrayList;
import java.util.List;


/**
 * Clase encargada de generar una recomendación de outfit basado en el clima actual
 * , el estilo seleccionado y las preferencias del usuario
 */
public class OutfitService {

    // Constantes de temperatura
    private static final double VERY_COLD = 5.0;
    private static final double COLD = 14.0;
    private static final double MILD = 22.0;
    private static final double HOT = 28.0;
    // > 28°C es MUY CALUROSO

    // Constantes de humedad
    private static final int HIGH_HUMIDITY = 70;    //alta humedad
    private static final int LOW_HUMIDITY = 30;     // baja humedad

    public OutfitRecommendation getOutfitRecommendation(CurrentWeather weather,
                                                        OutfitRecommendation.Style style,
                                                        UserPreferences userPreferences) {
        double temperature = weather.getTemperature();
        int humidity = weather.getHumidity();
        String condition = weather.getWeatherCondition().toLowerCase();
        boolean isRainy = condition.contains("rain") || condition.contains("shower") || condition.contains("lluvia");
        boolean isSnowy = condition.contains("snow") || condition.contains("nieve");
        boolean isCloudy = condition.contains("cloud") || condition.contains("nublado");
        boolean isWindy = condition.contains("wind") || condition.contains("viento");

        // Preferencias del usuario
        UserPreferences.Gender gender = userPreferences.getGender();
        UserPreferences.Tolerance coldTolerance = userPreferences.getColdTolerance();
        UserPreferences.Tolerance heatTolerance = userPreferences.getHeatTolerance();

        // Temperatura según las tolerancias personales
        double adjustedTemperature = adjustTemperatureByTolerances(temperature, coldTolerance, heatTolerance);

        // Ajustar según la humedad
        adjustedTemperature = adjustTemperatureByHumidity(adjustedTemperature, humidity);

        List<String> tops = new ArrayList<>();
        List<String> bottoms = new ArrayList<>();
        List<String> shoes = new ArrayList<>();
        List<String> outerwear = new ArrayList<>();
        List<String> accessories = new ArrayList<>();

        // Usamos adjustedTemperature para las decisiones
        if (adjustedTemperature <= VERY_COLD) {
            generateVeryColdOutfit(style, tops, bottoms, shoes, outerwear, accessories, gender);
        } else if (adjustedTemperature <= COLD) {
            generateColdOutfit(style, tops, bottoms, shoes, outerwear, accessories, gender);
        } else if (adjustedTemperature <= MILD) {
            generateMildOutfit(style, tops, bottoms, shoes, outerwear, accessories, gender);
        } else if (adjustedTemperature <= HOT) {
            generateHotOutfit(style, tops, bottoms, shoes, outerwear, accessories, gender);
        } else {
            generateVeryHotOutfit(style, tops, bottoms, shoes, outerwear, accessories, gender);
        }

        if (humidity >= HIGH_HUMIDITY) { // Si la humedad es alta
            addHighHumidityItems(adjustedTemperature, accessories); // Agregamos elementos
        }

        if (isRainy) {
            addRainyDayItems(style, outerwear, accessories, shoes, gender);
        }

        if (isSnowy) {
            addSnowyDayItems(style, outerwear, accessories, shoes, gender);
        }

        if (isWindy) {
            addWindyDayItems(style, accessories, gender);
        }

        return new OutfitRecommendation(tops, bottoms, shoes, outerwear, accessories, style);
    }

     // Ajusta la temperatura según las tolerancias personales al frío y calor del usuario
    private double adjustTemperatureByTolerances(double temperature,
                                                 UserPreferences.Tolerance coldTolerance,
                                                 UserPreferences.Tolerance heatTolerance) {
        double adjustedTemp = temperature;

        // Ajustar según tolerancia al frío
        if (temperature < 15.0) {  // Solo aplicar para temperaturas frías
            if (coldTolerance == UserPreferences.Tolerance.LOW) {
                adjustedTemp -= 3.0;  // Se siente más frío para quien tolera poco el frío
            } else if (coldTolerance == UserPreferences.Tolerance.HIGH) {
                adjustedTemp += 2.0;  // Se siente menos frío para quien tolera bien el frío
            }
        }

        // Ajustar según tolerancia al calor
        if (temperature > 22.0) {  // Solo aplicar para temperaturas cálidas
            if (heatTolerance == UserPreferences.Tolerance.LOW) {
                adjustedTemp += 3.0;  // Se siente más calor para quien tolera poco el calor
            } else if (heatTolerance == UserPreferences.Tolerance.HIGH) {
                adjustedTemp -= 2.0;  // Se siente menos calor para quien tolera bien el calor
            }
        }

        return adjustedTemp;
    }

    // Método que genera un outfit para clima muy frío
    private void generateVeryColdOutfit(OutfitRecommendation.Style style,
                                        List<String> tops, List<String> bottoms,
                                        List<String> shoes, List<String> outerwear,
                                        List<String> accessories, UserPreferences.Gender gender) {
        // Accesorios comunes
        accessories.add("Gorro de lana");
        accessories.add("Bufanda gruesa");
        accessories.add("Guantes");

        switch (style) {
            case CASUAL:
                tops.add("Camiseta térmica de manga larga");

                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Jersey de lana mujer");
                    bottoms.add("Vaqueros");
                    shoes.add("Botas de invierno mujer");
                    outerwear.add("Abrigo de plumas mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Jersey de lana hombre");
                    bottoms.add("Vaqueros");
                    shoes.add("Botas de invierno hombre");
                    outerwear.add("Abrigo de plumas hombre");
                } else {
                    tops.add("Jersey de lana");
                    bottoms.add("Vaqueros");
                    shoes.add("Botas de invierno");
                    outerwear.add("Abrigo de plumas");
                }
                break;

            case SPORTY:
                tops.add("Camiseta térmica deportiva");

                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Sudadera polar mujer");
                    bottoms.add("Pantalón deportivo térmico");
                    shoes.add("Zapatillas deportivas mujer");
                    outerwear.add("Chaqueta deportiva mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Sudadera polar hombre");
                    bottoms.add("Pantalón deportivo térmico");
                    shoes.add("Zapatillas deportivas hombre");
                    outerwear.add("Chaqueta deportiva hombre");
                } else {
                    tops.add("Sudadera polar");
                    bottoms.add("Pantalón deportivo térmico");
                    shoes.add("Zapatillas deportivas");
                    outerwear.add("Chaqueta deportiva");
                }
                break;

            case FORMAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Blusa de manga larga");
                    tops.add("Jersey formal mujer");
                    bottoms.add("Vaquero");
                    shoes.add("Zapatos formales mujer");
                    outerwear.add("Abrigo de lana mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camisa de manga larga");
                    tops.add("Jersey formal hombre");
                    bottoms.add("Vaquero");
                    shoes.add("Zapatos formales hombre");
                    outerwear.add("Abrigo de lana hombre");
                } else {
                    tops.add("Camisa de manga larga");
                    tops.add("Jersey formal");
                    bottoms.add("Vaquero");
                    shoes.add("Zapatos formales");
                    outerwear.add("Abrigo de lana");
                }
                break;
        }
    }

    // Método que genera un outfit para clima frío
    private void generateColdOutfit(OutfitRecommendation.Style style,
                                    List<String> tops, List<String> bottoms,
                                    List<String> shoes, List<String> outerwear,
                                    List<String> accessories, UserPreferences.Gender gender) {
        accessories.add("Bufanda ligera");

        switch (style) {
            case CASUAL:
                tops.add("Camiseta de manga larga");

                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Jersey mujer");
                    bottoms.add("Vaqueros");
                    shoes.add("Botines mujer");
                    outerwear.add("Chaqueta mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Jersey hombre");
                    bottoms.add("Vaqueros");
                    shoes.add("Botines hombre");
                    outerwear.add("Chaqueta hombre");
                } else {
                    tops.add("Jersey");
                    bottoms.add("Vaqueros");
                    shoes.add("Botines");
                    outerwear.add("Chaqueta");
                }
                break;

            case SPORTY:
                tops.add("Camiseta técnica de manga larga");
                bottoms.add("Pantalón deportivo");
                outerwear.add("Chaqueta cortavientos");

                if (gender == UserPreferences.Gender.FEMALE) {
                    shoes.add("Zapatillas deportivas mujer");
                    accessories.add("Gorra deportiva");
                } else if (gender == UserPreferences.Gender.MALE) {
                    shoes.add("Zapatillas deportivas hombre");
                    accessories.add("Gorra deportiva");
                } else {
                    shoes.add("Zapatillas deportivas");
                    accessories.add("Gorra deportiva");
                }
                break;

            case FORMAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Blusa");
                    tops.add("Jersey fino mujer");
                    bottoms.add("Vaquero");
                    shoes.add("Zapatos formales mujer");
                    outerwear.add("Blazer mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camisa de vestir");
                    tops.add("Jersey fino hombre");
                    bottoms.add("Vaquero");
                    shoes.add("Zapatos formales hombre");
                    outerwear.add("Blazer hombre");
                } else {
                    tops.add("Camisa de vestir");
                    tops.add("Jersey fino");
                    bottoms.add("Vaquero");
                    shoes.add("Zapatos formales");
                    outerwear.add("Blazer");
                }
                break;
        }
    }

    // Método que genera un outfit para clima normal
    private void generateMildOutfit(OutfitRecommendation.Style style,
                                    List<String> tops, List<String> bottoms,
                                    List<String> shoes, List<String> outerwear,
                                    List<String> accessories, UserPreferences.Gender gender) {
        switch (style) {
            case CASUAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Camiseta de algodón mujer");
                    bottoms.add("Pantalón casual");
                    shoes.add("Zapatillas casuales mujer");
                    outerwear.add("Chaqueta mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camiseta de algodón hombre");
                    bottoms.add("Pantalón casual");
                    shoes.add("Zapatillas casuales hombre");
                    outerwear.add("Chaqueta hombre");
                } else {
                    tops.add("Camiseta de algodón");
                    bottoms.add("Pantalón casual");
                    shoes.add("Zapatillas casuales");
                    outerwear.add("Chaqueta");
                }
                break;

            case SPORTY:
                accessories.add("Gorra");

                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Camiseta técnica mujer");
                    bottoms.add("Leggings");
                    shoes.add("Zapatillas deportivas mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camiseta técnica hombre");
                    bottoms.add("Pantalón corto deportivo");
                    shoes.add("Zapatillas deportivas hombre");
                } else {
                    tops.add("Camiseta técnica");
                    bottoms.add("Leggings");
                    shoes.add("Zapatillas deportivas");
                }
                break;

            case FORMAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Camisa elegante mujer");
                    bottoms.add("Falda formal");
                    shoes.add("Zapatos formales mujer");
                    outerwear.add("Blazer mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camisa elegante hombre");
                    bottoms.add("Vaquero");
                    shoes.add("Zapatos formales hombre");
                    outerwear.add("Blazer hombre");
                } else {
                    tops.add("Camisa elegante");
                    bottoms.add("Vaquero");
                    shoes.add("Zapatos formales");
                    outerwear.add("Blazer");
                }
                break;
        }
    }

    // Método que genera un outfit para clima caluroso
    private void generateHotOutfit(OutfitRecommendation.Style style,
                                   List<String> tops, List<String> bottoms,
                                   List<String> shoes, List<String> outerwear,
                                   List<String> accessories, UserPreferences.Gender gender) {
        accessories.add("Gafas de sol");

        switch (style) {
            case CASUAL:
                accessories.add("Sombrero");

                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Camiseta de tirantes mujer");
                    bottoms.add("Pantalón corto mujer");
                    shoes.add("Sandalias mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camiseta de manga corta hombre");
                    bottoms.add("Pantalón corto hombre");
                    shoes.add("Sandalias hombre");
                } else {
                    tops.add("Camiseta de manga corta");
                    bottoms.add("Pantalón corto");
                    shoes.add("Sandalias");
                }
                break;

            case SPORTY:
                accessories.add("Gorra con visera");

                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Camiseta técnica sin mangas mujer");
                    bottoms.add("Pantalón corto deportivo");
                    shoes.add("Zapatillas deportivas mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camiseta técnica hombre");
                    bottoms.add("Pantalón corto deportivo");
                    shoes.add("Zapatillas deportivas hombre");
                } else {
                    tops.add("Camiseta técnica");
                    bottoms.add("Pantalón corto deportivo");
                    shoes.add("Zapatillas deportivas");
                }
                break;

            case FORMAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Blusa sin mangas mujer");
                    bottoms.add("Pantalón Casual");
                    shoes.add("Zapatos formales mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camisa de manga corta formal hombre");
                    bottoms.add("Pantalón Casual");
                    shoes.add("Zapatos formales hombre");
                } else {
                    tops.add("Camisa ligera formal");
                    bottoms.add("Pantalón Casual");
                    shoes.add("Zapatos formales");
                }
                break;
        }
    }

    // Método que genera un outfit para clima muy caluroso
    private void generateVeryHotOutfit(OutfitRecommendation.Style style,
                                       List<String> tops, List<String> bottoms,
                                       List<String> shoes, List<String> outerwear,
                                       List<String> accessories, UserPreferences.Gender gender) {
        accessories.add("Gafas de sol");
        accessories.add("Protección solar");

        switch (style) {
            case CASUAL:
                accessories.add("Sombrero");

                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Top ligero mujer");
                    bottoms.add("Pantalón corto mujer");
                    shoes.add("Sandalias mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camiseta de tirantes hombre");
                    bottoms.add("Pantalón corto hombre");
                    shoes.add("Sandalias hombre");
                } else {
                    tops.add("Camiseta de tirantes");
                    bottoms.add("Pantalón corto");
                    shoes.add("Sandalias");
                }
                break;

            case SPORTY:
                accessories.add("Gorra deportiva");

                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Top deportivo mujer");
                    bottoms.add("Pantalón corto deportivo");
                    shoes.add("Zapatillas deportivas mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camiseta de tirantes deportiva hombre");
                    bottoms.add("Pantalón corto deportivo");
                    shoes.add("Zapatillas deportivas hombre");
                } else {
                    tops.add("Camiseta técnica ligera");
                    bottoms.add("Pantalón corto deportivo");
                    shoes.add("Zapatillas deportivas");
                }
                break;

            case FORMAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Blusa sin mangas mujer");
                    bottoms.add("Pantalón de lino");
                    shoes.add("Zapatos formales mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camisa de manga corta formal hombre");
                    bottoms.add("Pantalón de lino");
                    shoes.add("Zapatos formales hombre");
                } else {
                    tops.add("Camisa de lino");
                    bottoms.add("Pantalón de lino");
                    shoes.add("Zapatos formales");
                }
                break;
        }
    }

    // Método que genera un outfit para clima con lluvia
    private void addRainyDayItems(OutfitRecommendation.Style style,
                                  List<String> outerwear,
                                  List<String> accessories,
                                  List<String> shoes,
                                  UserPreferences.Gender gender) {
        accessories.add("Paraguas");

        switch (style) {
            case CASUAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    outerwear.add("Impermeable mujer");
                    shoes.clear();
                    shoes.add("Botas impermeables mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    outerwear.add("Impermeable hombre");
                    shoes.clear();
                    shoes.add("Botas impermeables hombre");
                } else {
                    outerwear.add("Impermeable");
                    shoes.clear();
                    shoes.add("Botas impermeables");
                }
                break;

            case SPORTY:
                outerwear.add("Chaqueta impermeable deportiva");

                if (gender == UserPreferences.Gender.FEMALE) {
                    shoes.clear();
                    shoes.add("Zapatillas impermeables mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    shoes.clear();
                    shoes.add("Zapatillas impermeables hombre");
                } else {
                    shoes.clear();
                    shoes.add("Zapatillas impermeables");
                }
                break;

            case FORMAL:
                outerwear.add("Gabardina");

                if (gender == UserPreferences.Gender.FEMALE) {
                    shoes.clear();
                    shoes.add("Zapatillas impermeables mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    shoes.clear();
                    shoes.add("Zapatillas impermeables hombre");
                } else {
                    shoes.clear();
                    shoes.add("Zapatillas impermeables");
                }
                break;
        }
    }

    // Método que genera un outfit para clima con nieve
    private void addSnowyDayItems(OutfitRecommendation.Style style,
                                  List<String> outerwear,
                                  List<String> accessories,
                                  List<String> shoes,
                                  UserPreferences.Gender gender) {
        accessories.add("Guantes térmicos");
        accessories.add("Gorro de lana");
        accessories.add("Bufanda gruesa");

        if (gender == UserPreferences.Gender.FEMALE) {
            shoes.clear();
            shoes.add("Botas de nieve mujer");
        } else if (gender == UserPreferences.Gender.MALE) {
            shoes.clear();
            shoes.add("Botas de nieve hombre");
        } else {
            shoes.clear();
            shoes.add("Botas de nieve");
        }

        switch (style) {
            case CASUAL:
            case SPORTY:
                if (gender == UserPreferences.Gender.FEMALE) {
                    outerwear.add("Abrigo térmico mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    outerwear.add("Abrigo térmico hombre");
                } else {
                    outerwear.add("Abrigo térmico");
                }
                break;

            case FORMAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    outerwear.add("Abrigo de lana mujer");
                } else if (gender == UserPreferences.Gender.MALE) {
                    outerwear.add("Abrigo de lana hombre");
                } else {
                    outerwear.add("Abrigo de lana");
                }
                break;
        }
    }

    // Método que genera un outfit para clima con viento
    private void addWindyDayItems(OutfitRecommendation.Style style, List<String> accessories, UserPreferences.Gender gender) {
        if (style == OutfitRecommendation.Style.CASUAL || style == OutfitRecommendation.Style.FORMAL) {
            accessories.add("Bufanda gruesa");

            if (gender == UserPreferences.Gender.FEMALE && style == OutfitRecommendation.Style.CASUAL) {
                accessories.add("Bufanda fina");
            }
        } else {
            accessories.add("Braga de cuello");
        }
    }

    /**
     * Ajusta la temperatura percibida según el nivel de humedad
     * En climas fríos, alta humedad = más frío
     * En climas cálidos, alta humedad = más calor
     */
    private double adjustTemperatureByHumidity(double temperature, int humidity) {

        if (temperature < 15.0) {
            if (humidity >= HIGH_HUMIDITY) {
                return temperature - 2.0; // Se siente 2 grados más frío
            } else if (humidity <= LOW_HUMIDITY) {
                return temperature + 1.0; // Se siente 1 grado más cálido
            }
        } else if (temperature > 22.0) {

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
            // Para clima frío y húmedo, se añaden capas impermeables
            accessories.add("Ropa interior térmica");
        } else if (adjustedTemperature >= HOT) {
            // Para clima caluroso y húmedo, se añade ropa transpirable
            accessories.add("Ropa de tejidos transpirables");
        }
    }
}