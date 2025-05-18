package com.example.weatherforecast.model;

import java.util.List;

/**
 * Clase que representa una recomendación de outfit
 */
public class OutfitRecommendation {
    public enum Style {
        CASUAL,
        SPORTY,
        FORMAL
    }

    private final List<String> topItems;      // Camisetas, blusas, camisas, etc.
    private final List<String> bottomItems;   // Pantalones, faldas, shorts, etc.
    private final List<String> footwear;      // Zapatos, zapatillas, botas, etc.
    private final List<String> outerWear;     // Chaquetas, abrigos, sudaderas, etc.
    private final List<String> accessories;   // Gorras, bufandas, gafas, etc.
    private final Style style;                // Estilo del outfit

    public OutfitRecommendation(List<String> topItems, List<String> bottomItems, List<String> footwear,
                                List<String> outerWear, List<String> accessories, Style style) {
        this.topItems = topItems;
        this.bottomItems = bottomItems;
        this.footwear = footwear;
        this.outerWear = outerWear;
        this.accessories = accessories;
        this.style = style;
    }

    // Getters
    public List<String> getTopItems() {
        return topItems;
    }
    public List<String> getBottomItems() {
        return bottomItems;
    }
    public List<String> getFootwear() {
        return footwear;
    }
    public List<String> getOuterWear() {
        return outerWear;
    }
    public List<String> getAccessories() {
        return accessories;
    }
    public Style getStyle() {
        return style;
    }

}