package com.example.weatherforecast.service;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.weatherforecast.R;
import com.example.weatherforecast.util.OutfitImageMapper;
import com.example.weatherforecast.model.OutfitRecommendation;

import java.util.List;

/**
 * Clase que muestra el outfit mediante imágenes en un contenedor
 */
public class OutfitDisplayHelper {

    private final Context context;
    private final OutfitImageMapper imageMapper;
    private final LayoutInflater inflater;

    public OutfitDisplayHelper(Context context) {
        this.context = context;
        this.imageMapper = new OutfitImageMapper();
        this.inflater = LayoutInflater.from(context);
    }

    public void displayOutfitWithImages(ViewGroup container, OutfitRecommendation recommendation) {
        container.removeAllViews();

        // Creamos la vista que contendrá las imágenes del outfit
        View outfitView = inflater.inflate(R.layout.outfit_image_display, container, false);

        // Obtenemos los contenedores para cada tipo de prenda
        LinearLayout topsContainer = outfitView.findViewById(R.id.topsContainer);
        LinearLayout bottomsContainer = outfitView.findViewById(R.id.bottomsContainer);
        LinearLayout shoesContainer = outfitView.findViewById(R.id.shoesContainer);
        LinearLayout outerwearContainer = outfitView.findViewById(R.id.outerwearContainer);
        LinearLayout accessoriesContainer = outfitView.findViewById(R.id.accessoriesContainer);

        // Añadimos las imágenes correspondientes a cada categoría
        addItemsImages(topsContainer, recommendation.getTopItems(), ItemType.TOP);
        addItemsImages(bottomsContainer, recommendation.getBottomItems(), ItemType.BOTTOM);
        addItemsImages(shoesContainer, recommendation.getFootwear(), ItemType.FOOTWEAR);
        addItemsImages(outerwearContainer, recommendation.getOuterWear(), ItemType.OUTERWEAR);
        addItemsImages(accessoriesContainer, recommendation.getAccessories(), ItemType.ACCESSORY);

        container.addView(outfitView);
    }

    // Método encargado de añadir las imágenes de las prendas al contenedor
    private void addItemsImages(ViewGroup container, List<String> items, ItemType type) {
        container.removeAllViews();

        if (items == null || items.isEmpty()) {
            return;
        }

        // Iterar sobre todos los elementos de la lista
        for (String item : items) {
            ImageView itemImageView = new ImageView(context);

            // Configuramos parámetros del ImageView
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(100),  // Ancho fijo
                    dpToPx(100)   // Alto fijo
            );

            params.gravity = Gravity.CENTER;

            if (type == ItemType.ACCESSORY) {
                params.setMargins(dpToPx(15), 0, dpToPx(15), 0); // Márgenes grandes para accesorios
            } else {
                params.setMargins(dpToPx(4), 0, dpToPx(4), 0); // Márgenes normales para el resto
            }

            itemImageView.setLayoutParams(params);

            // Obtenemos la imagen según el tipo de prenda
            int imageResource = getResourceForItem(item, type);
            itemImageView.setImageResource(imageResource);
            itemImageView.setContentDescription(item);

            // Añadimos la imagen al contenedor
            container.addView(itemImageView);
        }
    }

    // Método para convertir dp a píxeles
    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private int getResourceForItem(String itemDescription, ItemType type) {
        switch (type) {
            case TOP:
                return imageMapper.getTopItemResource(itemDescription);
            case BOTTOM:
                return imageMapper.getBottomItemResource(itemDescription);
            case FOOTWEAR:
                return imageMapper.getFootwearResource(itemDescription);
            case OUTERWEAR:
                return imageMapper.getOuterwearResource(itemDescription);
            case ACCESSORY:
                return imageMapper.getAccessoriesResource(itemDescription);
            default:
                return R.drawable.zapatos_formales_mujer;
        }
    }

    // Enum para identificar el tipo de prenda
    private enum ItemType {
        TOP, BOTTOM, FOOTWEAR, OUTERWEAR, ACCESSORY
    }
}
