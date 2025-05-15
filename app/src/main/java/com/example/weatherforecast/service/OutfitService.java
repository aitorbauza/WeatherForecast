package com.example.weatherforecast.service;

import com.example.weatherforecast.model.OutfitRecommendation;
import com.example.weatherforecast.model.CurrentWeather;
import com.example.weatherforecast.model.UserPreferences;

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
     * Genera una recomendación de outfit basado en el clima actual, el estilo seleccionado
     * y las preferencias del usuario
     */
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

        // Obtener preferencias del usuario
        UserPreferences.Gender gender = userPreferences.getGender();
        UserPreferences.Tolerance coldTolerance = userPreferences.getColdTolerance();
        UserPreferences.Tolerance heatTolerance = userPreferences.getHeatTolerance();

        // Ajustar la temperatura según las tolerancias personales
        double adjustedTemperature = adjustTemperatureByTolerances(temperature, coldTolerance, heatTolerance);

        // Ajustar según la humedad (original)
        adjustedTemperature = adjustTemperatureByHumidity(adjustedTemperature, humidity);

        List<String> tops = new ArrayList<>();
        List<String> bottoms = new ArrayList<>();
        List<String> shoes = new ArrayList<>();
        List<String> outerwear = new ArrayList<>();
        List<String> accessories = new ArrayList<>();

        // Ahora usamos adjustedTemperature para las decisiones
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

        // Añadir recomendaciones específicas de humedad
        if (humidity >= HIGH_HUMIDITY) {
            addHighHumidityItems(adjustedTemperature, accessories);
        }

        // El resto del método permanece similar
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

    /**
     * Ajusta la temperatura según las tolerancias personales al frío y calor del usuario
     */
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

    private void generateVeryColdOutfit(OutfitRecommendation.Style style,
                                        List<String> tops, List<String> bottoms,
                                        List<String> shoes, List<String> outerwear,
                                        List<String> accessories, UserPreferences.Gender gender) {
        // Accesorios comunes para clima muy frío
        accessories.addAll(Arrays.asList("Gorro de lana", "Bufanda gruesa", "Guantes"));

        switch (style) {
            case CASUAL:
                tops.add("Camiseta térmica de manga larga");
                tops.add("Suéter grueso");

                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Jersey de cuello alto");
                    bottoms.add("Jeans gruesos o leggings térmicos");
                    shoes.add("Botas de invierno acolchadas");
                    accessories.add("Orejeras acolchadas");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Jersey de lana");
                    bottoms.add("Jeans gruesos o pantalón de pana");
                    shoes.add("Botas de montaña");
                } else {
                    bottoms.add("Jeans gruesos");
                    shoes.add("Botas de invierno");
                }

                outerwear.add("Abrigo de plumas");
                break;

            case SPORTY:
                tops.add("Camiseta térmica deportiva");

                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Sudadera polar ajustada");
                    bottoms.add("Mallas térmicas");
                    shoes.add("Zapatillas de trail con aislamiento");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Sudadera polar");
                    bottoms.add("Pantalón deportivo térmico");
                    shoes.add("Zapatillas trail resistentes al frío");
                } else {
                    tops.add("Sudadera polar");
                    bottoms.add("Pantalón deportivo térmico");
                    shoes.add("Zapatillas deportivas resistentes al frío");
                }

                outerwear.add("Chaqueta deportiva aislante");
                break;

            case FORMAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Blusa de manga larga");
                    tops.add("Jersey de cachemira");
                    bottoms.add("Falda de lana con medias térmicas o pantalón de vestir forrado");
                    shoes.add("Botas elegantes de tacón bajo");
                    outerwear.add("Abrigo de lana entallado");
                    accessories.add("Fulard elegante");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camisa de manga larga");
                    tops.add("Chaleco");
                    tops.add("Jersey de lana");
                    bottoms.add("Pantalón de vestir de lana");
                    shoes.add("Zapatos Oxford con calcetines térmicos");
                    outerwear.add("Abrigo de lana largo");
                    accessories.add("Corbata de lana");
                } else {
                    tops.add("Camisa de manga larga");
                    tops.add("Jersey elegante");
                    bottoms.add("Pantalón de vestir de lana");
                    shoes.add("Zapatos formales con calcetines térmicos");
                    outerwear.add("Abrigo de lana");
                }
                break;
        }
    }

    private void generateColdOutfit(OutfitRecommendation.Style style,
                                    List<String> tops, List<String> bottoms,
                                    List<String> shoes, List<String> outerwear,
                                    List<String> accessories, UserPreferences.Gender gender) {
        accessories.add("Bufanda ligera");

        switch (style) {
            case CASUAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Camiseta de manga larga");
                    tops.add("Suéter ligero");
                    bottoms.add("Jeans o pantalón recto");
                    shoes.add("Botines o botas hasta el tobillo");
                    outerwear.add("Chaqueta acolchada");
                    accessories.add("Gorro de punto ligero");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camiseta de manga larga");
                    tops.add("Sudadera ligera");
                    bottoms.add("Jeans");
                    shoes.add("Botines o zapatillas altas");
                    outerwear.add("Chaqueta acolchada");
                } else {
                    tops.add("Camiseta de manga larga");
                    bottoms.add("Jeans");
                    shoes.add("Botines");
                    outerwear.add("Chaqueta acolchada");
                }
                break;

            case SPORTY:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Camiseta técnica de manga larga");
                    bottoms.add("Mallas o pantalón deportivo");
                    shoes.add("Zapatillas deportivas con calcetines térmicos");
                    outerwear.add("Chaqueta cortavientos");
                    accessories.add("Banda para las orejas");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camiseta técnica de manga larga");
                    bottoms.add("Pantalón deportivo");
                    shoes.add("Zapatillas deportivas con calcetines");
                    outerwear.add("Chaqueta cortavientos");
                    accessories.add("Gorro deportivo");
                } else {
                    tops.add("Camiseta técnica de manga larga");
                    bottoms.add("Pantalón deportivo");
                    shoes.add("Zapatillas deportivas");
                    outerwear.add("Chaqueta cortavientos");
                    accessories.add("Gorro deportivo");
                }
                break;

            case FORMAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Blusa o camisa elegante");
                    tops.add("Jersey fino o cárdigan");
                    bottoms.add("Pantalón de vestir o falda con medias");
                    shoes.add("Zapatos cerrados o botines de tacón");
                    outerwear.add("Blazer forrado o abrigo corto");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camisa de vestir");
                    tops.add("Jersey fino o chaleco");
                    bottoms.add("Pantalón de vestir");
                    shoes.add("Zapatos Oxford o Derby");
                    outerwear.add("Blazer forrado");
                    accessories.add("Pañuelo de bolsillo");
                } else {
                    tops.add("Camisa de vestir");
                    tops.add("Jersey fino");
                    bottoms.add("Pantalón de vestir");
                    shoes.add("Zapatos Oxford");
                    outerwear.add("Blazer forrado");
                }
                break;
        }
    }

    private void generateMildOutfit(OutfitRecommendation.Style style,
                                    List<String> tops, List<String> bottoms,
                                    List<String> shoes, List<String> outerwear,
                                    List<String> accessories, UserPreferences.Gender gender) {
        switch (style) {
            case CASUAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Camiseta o blusa de algodón");
                    bottoms.add("Jeans, pantalón chino o falda casual");
                    shoes.add("Zapatillas casuales o bailarinas");
                    outerwear.add("Cárdigan o chaqueta ligera");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camiseta o polo de algodón");
                    bottoms.add("Jeans o pantalón chino");
                    shoes.add("Zapatillas casuales o mocasines");
                    outerwear.add("Sudadera o chaqueta ligera");
                } else {
                    tops.add("Camiseta de algodón");
                    bottoms.add("Jeans o pantalón chino");
                    shoes.add("Zapatillas casuales");
                    outerwear.add("Sudadera o chaqueta ligera");
                }
                break;

            case SPORTY:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Camiseta técnica o top deportivo");
                    bottoms.add("Mallas o pantalón corto deportivo");
                    shoes.add("Zapatillas deportivas ligeras");
                    outerwear.add("Chaqueta deportiva ligera");
                    accessories.add("Visera o gorra");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camiseta técnica");
                    bottoms.add("Pantalón corto deportivo");
                    shoes.add("Zapatillas deportivas ligeras");
                    outerwear.add("Sudadera con capucha");
                    accessories.add("Gorra");
                } else {
                    tops.add("Camiseta técnica");
                    bottoms.add("Pantalón corto deportivo o leggings");
                    shoes.add("Zapatillas deportivas ligeras");
                    outerwear.add("Sudadera con capucha");
                    accessories.add("Gorra");
                }
                break;

            case FORMAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Blusa o camisa elegante");
                    bottoms.add("Pantalón de vestir, falda o vestido");
                    shoes.add("Zapatos de tacón medio o bailarinas elegantes");
                    outerwear.add("Blazer ligero");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camisa de manga larga");
                    bottoms.add("Pantalón de vestir ligero");
                    shoes.add("Zapatos de vestir");
                    outerwear.add("Americana ligera");
                } else {
                    tops.add("Camisa de manga larga");
                    bottoms.add("Pantalón de vestir ligero");
                    shoes.add("Zapatos de vestir");
                    outerwear.add("Americana ligera");
                }
                break;
        }
    }

    private void generateHotOutfit(OutfitRecommendation.Style style,
                                   List<String> tops, List<String> bottoms,
                                   List<String> shoes, List<String> outerwear,
                                   List<String> accessories, UserPreferences.Gender gender) {
        accessories.add("Gafas de sol");

        switch (style) {
            case CASUAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Camiseta de tirantes o blusa ligera");
                    bottoms.add("Falda, vestido veraniego o pantalón corto");
                    shoes.add("Sandalias o alpargatas");
                    accessories.add("Sombrero de ala ancha");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camiseta de manga corta o polo");
                    bottoms.add("Bermudas o pantalón corto ligero");
                    shoes.add("Sandalias o zapatillas ligeras");
                    accessories.add("Gorra");
                } else {
                    tops.add("Camiseta de manga corta");
                    bottoms.add("Pantalón corto o falda");
                    shoes.add("Sandalias o zapatillas ligeras");
                }
                break;

            case SPORTY:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Top deportivo o camiseta técnica sin mangas");
                    bottoms.add("Mallas cortas o pantalón corto deportivo");
                    shoes.add("Zapatillas deportivas ligeras y transpirables");
                    accessories.add("Visera y banda para el pelo");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camiseta técnica transpirable");
                    bottoms.add("Pantalón corto deportivo ligero");
                    shoes.add("Zapatillas deportivas ligeras");
                    accessories.add("Gorra con visera o cinta para la frente");
                } else {
                    tops.add("Camiseta técnica transpirable");
                    bottoms.add("Pantalón corto deportivo");
                    shoes.add("Zapatillas deportivas ligeras");
                    accessories.add("Gorra con visera");
                }
                break;

            case FORMAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Blusa ligera de materiales naturales");
                    bottoms.add("Falda o pantalón de vestir ligero");
                    shoes.add("Sandalias elegantes o zapatos abiertos");
                    accessories.add("Pañuelo ligero");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camisa de manga corta o larga arremangada");
                    bottoms.add("Pantalón de vestir ligero");
                    shoes.add("Zapatos ligeros o mocasines");
                } else {
                    tops.add("Camisa de manga corta o larga arremangada");
                    bottoms.add("Pantalón de vestir ligero");
                    shoes.add("Zapatos ligeros");
                }
                break;
        }
    }

    private void generateVeryHotOutfit(OutfitRecommendation.Style style,
                                       List<String> tops, List<String> bottoms,
                                       List<String> shoes, List<String> outerwear,
                                       List<String> accessories, UserPreferences.Gender gender) {
        accessories.add("Gafas de sol");
        accessories.add("Protección solar");

        switch (style) {
            case CASUAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Top ligero, camiseta sin mangas o blusa ligera");
                    bottoms.add("Vestido veraniego, falda ligera o shorts");
                    shoes.add("Sandalias o chanclas");
                    accessories.add("Sombrero de ala ancha o pamela");
                    accessories.add("Abanico");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camiseta sin mangas o camisa muy ligera");
                    bottoms.add("Bermudas o pantalón corto ligero");
                    shoes.add("Sandalias o chanclas");
                    accessories.add("Sombrero o gorra");
                } else {
                    tops.add("Camiseta sin mangas o de tejido ligero");
                    bottoms.add("Pantalón corto o falda ligera");
                    shoes.add("Sandalias");
                    accessories.add("Sombrero o gorra para protección solar");
                }
                break;

            case SPORTY:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Top deportivo o camiseta técnica ultraligera");
                    bottoms.add("Pantalón corto deportivo muy ligero o falda-pantalón");
                    shoes.add("Zapatillas deportivas transpirables o sandalias deportivas");
                    accessories.add("Visera y muñequeras absorbentes");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camiseta técnica sin mangas o de manga corta ultraligera");
                    bottoms.add("Pantalón corto deportivo ligero");
                    shoes.add("Zapatillas deportivas ultratranspirables");
                    accessories.add("Gorra técnica con protección UV");
                } else {
                    tops.add("Camiseta técnica de manga corta ultraligera");
                    bottoms.add("Pantalón corto deportivo ligero");
                    shoes.add("Zapatillas deportivas transpirables");
                    accessories.add("Gorra o visera deportiva");
                }
                break;

            case FORMAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    tops.add("Blusa sin mangas de tejidos naturales");
                    bottoms.add("Falda ligera, vestido o pantalón de lino");
                    shoes.add("Sandalias elegantes");
                    accessories.add("Abanico pequeño");
                } else if (gender == UserPreferences.Gender.MALE) {
                    tops.add("Camisa de manga corta o de lino");
                    bottoms.add("Pantalón ligero de lino o algodón");
                    shoes.add("Zapatos ligeros sin calcetines o con invisibles");
                } else {
                    tops.add("Camisa de lino o algodón fino");
                    bottoms.add("Pantalón ligero de lino o algodón");
                    shoes.add("Zapatos ligeros sin calcetines o con calcetines invisibles");
                }
                break;
        }
    }

    private void addRainyDayItems(OutfitRecommendation.Style style,
                                  List<String> outerwear,
                                  List<String> accessories,
                                  List<String> shoes,
                                  UserPreferences.Gender gender) {
        accessories.add("Paraguas");

        switch (style) {
            case CASUAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    outerwear.add("Impermeable o trench");
                    if (!shoes.contains("Botas de agua")) {
                        shoes.clear();
                        shoes.add("Botas de agua o botines impermeables");
                    }
                } else if (gender == UserPreferences.Gender.MALE) {
                    outerwear.add("Impermeable o chubasquero");
                    if (!shoes.contains("Botas impermeables")) {
                        shoes.clear();
                        shoes.add("Botas impermeables o zapatillas resistentes al agua");
                    }
                } else {
                    outerwear.add("Impermeable o chubasquero");
                    if (!shoes.contains("Botas impermeables")) {
                        shoes.clear();
                        shoes.add("Botas impermeables");
                    }
                }
                break;

            case SPORTY:
                outerwear.add("Chaqueta impermeable deportiva");
                if (!shoes.contains("Zapatillas impermeables")) {
                    shoes.clear();
                    shoes.add("Zapatillas impermeables");
                }
                if (gender == UserPreferences.Gender.FEMALE) {
                    accessories.add("Gorra impermeable");
                }
                break;

            case FORMAL:
                if (gender == UserPreferences.Gender.FEMALE) {
                    outerwear.add("Gabardina o trench elegante");
                } else {
                    outerwear.add("Gabardina");
                }

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
                                  List<String> shoes,
                                  UserPreferences.Gender gender) {
        accessories.add("Guantes térmicos");
        accessories.add("Gorro de lana");
        accessories.add("Bufanda gruesa");

        if (gender == UserPreferences.Gender.FEMALE) {
            accessories.add("Orejeras");
        }

        switch (style) {
            case CASUAL:
            case SPORTY:
                if (!shoes.contains("Botas de nieve")) {
                    shoes.clear();
                    shoes.add("Botas de nieve");
                }

                if (gender == UserPreferences.Gender.FEMALE && style == OutfitRecommendation.Style.CASUAL) {
                    outerwear.add("Abrigo largo de plumas");
                } else {
                    outerwear.add("Abrigo con aislante térmico");
                }
                break;

            case FORMAL:
                if (!shoes.contains("Botas de vestir impermeables")) {
                    shoes.clear();
                    shoes.add("Botas de vestir impermeables");
                }

                if (gender == UserPreferences.Gender.FEMALE) {
                    outerwear.add("Abrigo largo de lana con forro");
                } else if (gender == UserPreferences.Gender.MALE) {
                    outerwear.add("Abrigo de lana con forro");
                } else {
                    outerwear.add("Abrigo de lana con forro");
                }
                break;
        }
    }

    private void addWindyDayItems(OutfitRecommendation.Style style, List<String> accessories, UserPreferences.Gender gender) {
        if (style == OutfitRecommendation.Style.CASUAL || style == OutfitRecommendation.Style.FORMAL) {
            accessories.add("Bufanda para protección contra el viento");

            if (gender == UserPreferences.Gender.FEMALE && style == OutfitRecommendation.Style.CASUAL) {
                accessories.add("Pashmina o fular");
            }
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