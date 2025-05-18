package com.example.weatherforecast.service;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.weatherforecast.R;
import com.example.weatherforecast.model.OutfitImageMapper;
import com.example.weatherforecast.model.OutfitRecommendation;

import java.util.List;

public class OutfitDisplayHelper {

    private final Context context;
    private final OutfitImageMapper imageMapper;
    private final LayoutInflater inflater;

    public OutfitDisplayHelper(Context context) {
        this.context = context;
        this.imageMapper = new OutfitImageMapper();
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * Muestra el outfit mediante imágenes en un contenedor
     * @param container Contenedor donde se mostrarán las imágenes
     * @param recommendation Recomendación de outfit con las prendas
     */
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

    private void addItemsImages(ViewGroup container, List<String> items, ItemType type) {
        container.removeAllViews();

        if (items == null || items.isEmpty()) {
            return;
        }

        // Para simplificar, mostramos solo el primer elemento de cada categoría
        String item = items.get(0);
        ImageView itemImageView = new ImageView(context);

        // Configuramos parámetros del ImageView
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        itemImageView.setLayoutParams(params);

        // Obtenemos la imagen según el tipo de prenda
        int imageResource = getResourceForItem(item, type);
        itemImageView.setImageResource(imageResource);
        itemImageView.setContentDescription(item);

        // Añadimos la imagen al contenedor
        container.addView(itemImageView);
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
