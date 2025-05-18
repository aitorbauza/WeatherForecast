package com.example.weatherforecast.service;

import com.example.weatherforecast.model.OutfitRecommendation;
import java.util.List;

/**
 * Clase auxiliar para calcular la calificación de confort de un outfit
 */
public class OutfitRatingHelper {

    /**
     * Calcula el porcentaje de confort entre un outfit original y uno modificado
     * @param original El outfit original recomendado
     * @param modified El outfit modificado por el usuario
     * @return El porcentaje de confort (0-100)
     */
    public static int calculateComfortPercentage(OutfitRecommendation original, OutfitRecommendation modified) {
        if (original == null || modified == null) {
            return 100;
        }

        int totalDifferences = 0;

        // Compara cada categoría de prendas
        if (!compareItems(original.getTopItems(), modified.getTopItems())) {
            totalDifferences++;
        }

        if (!compareItems(original.getBottomItems(), modified.getBottomItems())) {
            totalDifferences++;
        }

        if (!compareItems(original.getFootwear(), modified.getFootwear())) {
            totalDifferences++;
        }

        if (!compareItems(original.getOuterWear(), modified.getOuterWear())) {
            totalDifferences++;
        }

        if (!compareItems(original.getAccessories(), modified.getAccessories())) {
            totalDifferences++;
        }

        // Calcula el porcentaje de confort (cada diferencia resta un 20%)
        return Math.max(0, 100 - (totalDifferences * 20));
    }

    /**
     * Genera un mensaje apropiado según el porcentaje de confort
     * @param comfortPercentage El porcentaje de confort (0-100)
     * @return Un mensaje descriptivo
     */
    public static String generateRatingMessage(int comfortPercentage) {
        if (comfortPercentage >= 80) {
            return "¡Excelente elección! Tu confort con este outfit será de un " + comfortPercentage + "%";
        } else if (comfortPercentage >= 60) {
            return "Buena elección. Tu confort con este outfit será de un " + comfortPercentage + "%";
        } else if (comfortPercentage >= 40) {
            return "Elección aceptable. Tu confort con este outfit será de un " + comfortPercentage + "%";
        } else if (comfortPercentage >= 20) {
            return "Podrías reconsiderar algunas prendas. Tu confort será de un " + comfortPercentage + "%";
        } else {
            return "Este outfit podría no ser muy confortable. Tu confort será de un " + comfortPercentage + "%";
        }
    }

    /**
     * Compara dos listas de prendas para ver si son iguales
     */
    private static boolean compareItems(List<String> original, List<String> modified) {
        if (original == null && modified == null) return true;
        if (original == null || modified == null) return false;
        if (original.size() != modified.size()) return false;

        for (int i = 0; i < original.size(); i++) {
            if (!original.get(i).equals(modified.get(i))) {
                return false;
            }
        }

        return true;
    }
}