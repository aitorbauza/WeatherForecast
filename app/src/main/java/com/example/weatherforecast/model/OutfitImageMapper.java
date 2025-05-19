package com.example.weatherforecast.model;

import androidx.annotation.DrawableRes;

import com.example.weatherforecast.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapeador que convierte el texto de la prenda a imágen
 */
public class OutfitImageMapper {

    // Maps para cada tipo de prenda
    private final Map<String, Integer> topItemsMap = new HashMap<>();
    private final Map<String, Integer> bottomItemsMap = new HashMap<>();
    private final Map<String, Integer> footwearMap = new HashMap<>();
    private final Map<String, Integer> outerwearMap = new HashMap<>();
    private final Map<String, Integer> accessoriesMap = new HashMap<>();

    // Constructor que inicializa todos los maps
    public OutfitImageMapper() {
        initTopItemsMap();
        initBottomItemsMap();
        initFootwearMap();
        initOuterwearMap();
        initAccessoriesMap();
    }

    //Prendas defaults
    @DrawableRes
    public int getTopItemResource(String description) {
        return topItemsMap.getOrDefault(description.toLowerCase(), R.drawable.top_mujer);
    }

    @DrawableRes
    public int getBottomItemResource(String description) {
        return bottomItemsMap.getOrDefault(description.toLowerCase(), R.drawable.pantalon_deportivo);
    }

    @DrawableRes
    public int getFootwearResource(String description) {
        return footwearMap.getOrDefault(description.toLowerCase(), R.drawable.zapatillas_deportivas_mujer);
    }

    @DrawableRes
    public int getOuterwearResource(String description) {
        return outerwearMap.getOrDefault(description.toLowerCase(), R.drawable.chaqueta_deportiva_mujer);
    }

    @DrawableRes
    public int getAccessoriesResource(String description) {
        return accessoriesMap.getOrDefault(description.toLowerCase(), R.drawable.gorra_con_visera);
    }

    // Mapeo de prendas superiores
    private void initTopItemsMap() {
        // Prendas casuales
        topItemsMap.put("camiseta de algodón", R.drawable.camiseta_algodon_hombre);
        topItemsMap.put("camiseta de algodón mujer", R.drawable.camiseta_algodon_mujer);
        topItemsMap.put("camiseta de algodón hombre", R.drawable.camiseta_algodon_hombre);

        topItemsMap.put("camiseta de manga larga", R.drawable.camiseta_manga_larga);
        topItemsMap.put("camiseta de manga corta", R.drawable.camiseta_manga_corta);
        topItemsMap.put("camiseta de manga corta hombre", R.drawable.camiseta_manga_corta_hombre);

        topItemsMap.put("camiseta de tirantes", R.drawable.camiseta_tirantes_hombre);
        topItemsMap.put("camiseta de tirantes mujer", R.drawable.camiseta_tirantes_mujer);
        topItemsMap.put("camiseta de tirantes hombre", R.drawable.camiseta_tirantes_hombre);

        topItemsMap.put("top ligero mujer", R.drawable.top_mujer);

        // Prendas térmicas y de abrigo
        topItemsMap.put("camiseta térmica de manga larga", R.drawable.camiseta_tecnica_manga_larga);

        topItemsMap.put("jersey de lana", R.drawable.jersey_sueter_mujer);
        topItemsMap.put("jersey de lana mujer", R.drawable.jersey_sueter_mujer);
        topItemsMap.put("jersey de lana hombre", R.drawable.jersey_sueter_hombre);

        topItemsMap.put("jersey fino", R.drawable.jersey_fino_hombre);
        topItemsMap.put("jersey fino mujer", R.drawable.jersey_fino_mujer);
        topItemsMap.put("jersey fino hombre", R.drawable.jersey_fino_hombre);

        // Prendas deportivas
        topItemsMap.put("camiseta técnica", R.drawable.camiseta_tecnica_hombre);
        topItemsMap.put("camiseta técnica mujer", R.drawable.camiseta_tecnica_mujer);
        topItemsMap.put("camiseta técnica hombre", R.drawable.camiseta_tecnica_hombre);

        topItemsMap.put("camiseta técnica de manga larga", R.drawable.camiseta_tecnica_manga_larga);
        topItemsMap.put("camiseta técnica sin mangas mujer", R.drawable.camiseta_tecnica_sin_mangas_mujer);
        topItemsMap.put("camiseta térmica deportiva", R.drawable.camiseta_termica_manga_larga);
        topItemsMap.put("top deportivo mujer", R.drawable.top_deportivo_mujer);
        topItemsMap.put("camiseta de tirantes deportiva hombre", R.drawable.camiseta_tirantes_deportivo_hombre);

        topItemsMap.put("sudadera polar", R.drawable.sudadera_hombre);
        topItemsMap.put("sudadera polar mujer", R.drawable.sudadera_mujer);
        topItemsMap.put("sudadera polar hombre", R.drawable.sudadera_hombre);

        // Prendas formales
        topItemsMap.put("camisa de vestir", R.drawable.camisa_de_vestir);
        topItemsMap.put("camisa de manga larga", R.drawable.camisa_de_manga_larga);

        topItemsMap.put("camisa elegante", R.drawable.camisa_elegante_hombre);
        topItemsMap.put("camisa elegante mujer", R.drawable.camisa_elegante_mujer);
        topItemsMap.put("camisa elegante hombre", R.drawable.camisa_elegante_hombre);

        topItemsMap.put("blusa", R.drawable.blusa_mujer);
        topItemsMap.put("blusa de manga larga", R.drawable.blusa_manga_larga_mujer);
        topItemsMap.put("blusa sin mangas mujer", R.drawable.blusa_sin_mangas_mujer);

        topItemsMap.put("jersey formal", R.drawable.jersey_formal_hombre);
        topItemsMap.put("jersey formal mujer", R.drawable.jersey_formal_mujer);
        topItemsMap.put("jersey formal hombre", R.drawable.jersey_formal_hombre);

        topItemsMap.put("camisa de manga corta formal hombre", R.drawable.camisa_manga_corta_formal_hombre);
        topItemsMap.put("camisa de lino", R.drawable.camisa_lino);
    }

    // Mapeo de prendas inferiores
    private void initBottomItemsMap() {
        // Pantalones casuales
        bottomItemsMap.put("vaqueros", R.drawable.vaquero);
        bottomItemsMap.put("pantalón casual", R.drawable.pantalon_casual);

        bottomItemsMap.put("pantalón corto mujer", R.drawable.pantalon_corto_mujer);
        bottomItemsMap.put("pantalón corto hombre", R.drawable.pantalon_corto_hombre);
        bottomItemsMap.put("pantalón corto", R.drawable.pantalon_corto_hombre);

        // Pantalones deportivos
        bottomItemsMap.put("pantalón deportivo", R.drawable.pantalon_deportivo);
        bottomItemsMap.put("pantalón deportivo térmico", R.drawable.pantalon_deportivo_termico);
        bottomItemsMap.put("pantalón corto deportivo", R.drawable.pantalon_corto_deportivo);
        bottomItemsMap.put("leggings", R.drawable.legging);

        // Pantalones formales
        bottomItemsMap.put("falda formal", R.drawable.falda);
        bottomItemsMap.put("pantalón de lino", R.drawable.pantalon_lino);
    }

    // Mapeo de calzado
    private void initFootwearMap() {
        // Calzado casual
        footwearMap.put("zapatillas casuales", R.drawable.zapatillas_casuales_mujer);
        footwearMap.put("zapatillas casuales mujer", R.drawable.zapatillas_casuales_mujer);
        footwearMap.put("zapatillas casuales hombre", R.drawable.zapatillas_casuales_hombre);

        footwearMap.put("sandalias", R.drawable.sandalia_mujer);
        footwearMap.put("sandalias mujer", R.drawable.sandalia_mujer);
        footwearMap.put("sandalias hombre", R.drawable.sandalia_hombre);

        // Botas y botines
        footwearMap.put("botas de invierno", R.drawable.botas_invierno_mujer);
        footwearMap.put("botas de invierno mujer", R.drawable.botas_invierno_mujer);
        footwearMap.put("botas de invierno hombre", R.drawable.botas_invierno_hombre);

        footwearMap.put("botines", R.drawable.botines_hombre);
        footwearMap.put("botines mujer", R.drawable.botines_mujer);
        footwearMap.put("botines hombre", R.drawable.botines_hombre);

        footwearMap.put("botas impermeables", R.drawable.botas_impermeables_mujer);
        footwearMap.put("botas impermeables mujer", R.drawable.botas_impermeables_mujer);
        footwearMap.put("botas impermeables hombre", R.drawable.botas_impermeables_hombre);

        footwearMap.put("botas de nieve", R.drawable.bota_nieve_mujer);
        footwearMap.put("botas de nieve mujer", R.drawable.bota_nieve_mujer);
        footwearMap.put("botas de nieve hombre", R.drawable.bota_nieve_hombre);

        // Calzado deportivo
        footwearMap.put("zapatillas deportivas", R.drawable.zapatillas_deportivas_mujer);
        footwearMap.put("zapatillas deportivas mujer", R.drawable.zapatillas_deportivas_mujer);
        footwearMap.put("zapatillas deportivas hombre", R.drawable.zapatillas_deportivas_hombre);

        footwearMap.put("zapatillas impermeables", R.drawable.zapatillas_impermeables_mujer);
        footwearMap.put("zapatillas impermeables mujer", R.drawable.zapatillas_impermeables_mujer);
        footwearMap.put("zapatillas impermeables hombre", R.drawable.zapatillas_impermeables_hombre);

        // Calzado formal
        footwearMap.put("zapatos formales", R.drawable.zapatos_formales_hombre);
        footwearMap.put("zapatos formales mujer", R.drawable.zapatos_formales_mujer);
        footwearMap.put("zapatos formales hombre", R.drawable.zapatos_formales_hombre);
    }

    // Mapeo de prendas exteriores
    private void initOuterwearMap() {
        // Chaquetas
        outerwearMap.put("chaqueta", R.drawable.chaqueta_hombre);
        outerwearMap.put("chaqueta mujer", R.drawable.chaqueta_mujer);
        outerwearMap.put("chaqueta hombre", R.drawable.chaqueta_hombre);

        outerwearMap.put("chaqueta cortavientos", R.drawable.chaqueta_cortavientos);

        // Abrigos
        outerwearMap.put("abrigo de plumas", R.drawable.abrigo_plumas_hombre);
        outerwearMap.put("abrigo de plumas mujer", R.drawable.abrigo_plumas_mujer);
        outerwearMap.put("abrigo de plumas hombre", R.drawable.abrigo_plumas_hombre);

        outerwearMap.put("abrigo de lana", R.drawable.abrigo_lana_hombre);
        outerwearMap.put("abrigo de lana mujer", R.drawable.abrigo_lana_mujer);
        outerwearMap.put("abrigo de lana hombre", R.drawable.abrigo_lana_hombre);

        outerwearMap.put("abrigo térmico", R.drawable.abrigo_termico_hombre);
        outerwearMap.put("abrigo térmico mujer", R.drawable.abrigo_termico_mujer);
        outerwearMap.put("abrigo térmico hombre", R.drawable.abrigo_termico_hombre);

        // Chaquetas deportivas
        outerwearMap.put("chaqueta deportiva", R.drawable.chaqueta_deportiva_mujer);
        outerwearMap.put("chaqueta deportiva mujer", R.drawable.chaqueta_deportiva_mujer);
        outerwearMap.put("chaqueta deportiva hombre", R.drawable.chaqueta_deportiva_hombre);

        outerwearMap.put("chaqueta impermeable deportiva", R.drawable.chaqueta_impermeable_deportiva);

        // Impermeables
        outerwearMap.put("impermeable", R.drawable.impermeable_hombre);
        outerwearMap.put("impermeable mujer", R.drawable.impermeable_mujer);
        outerwearMap.put("impermeable hombre", R.drawable.impermeable_hombre);

        // Blazers
        outerwearMap.put("blazer", R.drawable.blazer_mujer);
        outerwearMap.put("blazer mujer", R.drawable.blazer_mujer);
        outerwearMap.put("blazer hombre", R.drawable.blazer_hombre);

        outerwearMap.put("gabardina", R.drawable.gabardina_hombre);
    }

    // Mapeo de accesorios
    private void initAccessoriesMap() {
        // Sombreros y gorros
        accessoriesMap.put("gorro de lana", R.drawable.gorro_lana);
        accessoriesMap.put("sombrero", R.drawable.sombrero);
        accessoriesMap.put("gorra", R.drawable.gorra);
        accessoriesMap.put("gorra deportiva", R.drawable.gorra_deportiva);
        accessoriesMap.put("gorra con visera", R.drawable.gorra_con_visera);

        // Bufandas
        accessoriesMap.put("bufanda gruesa", R.drawable.bufanda_gruesa);
        accessoriesMap.put("bufanda ligera", R.drawable.bufanda_ligera);
        accessoriesMap.put("braga de cuello", R.drawable.braga_cuello);

        // Otros
        accessoriesMap.put("guantes", R.drawable.guantes);
        accessoriesMap.put("guantes térmicos", R.drawable.guantes_termicos);
        accessoriesMap.put("gafas de sol", R.drawable.gafas_de_sol);
        accessoriesMap.put("paraguas", R.drawable.paraguas);
        accessoriesMap.put("protección solar", R.drawable.proteccion_solar);
        accessoriesMap.put("ropa interior térmica", R.drawable.ropa_interior_termica);
        accessoriesMap.put("ropa de tejidos transpirables", R.drawable.ropa_tejido_transpirable);
    }

}