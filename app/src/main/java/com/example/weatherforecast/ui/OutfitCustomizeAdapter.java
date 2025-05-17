package com.example.weatherforecast.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforecast.R;
import com.example.weatherforecast.model.OutfitRecommendation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adaptador para personalizar los elementos del outfit
 */
public class OutfitCustomizeAdapter extends RecyclerView.Adapter<OutfitCustomizeAdapter.ViewHolder> {

    private final Context context;
    private final OutfitRecommendation originalOutfit;

    // Mapeo para mantener alternativas para cada categoría
    private final Map<OutfitCategory, List<String>> categoryAlternatives;

    // Mapeo para mantener las selecciones actuales
    private final Map<OutfitCategory, String> currentSelections;

    // Enumerar las posibles categorías de outfit
    public enum OutfitCategory {
        TOP, BOTTOM, FOOTWEAR, OUTERWEAR, ACCESSORIES
    }

    public OutfitCustomizeAdapter(Context context, OutfitRecommendation outfit) {
        this.context = context;
        this.originalOutfit = outfit;
        this.categoryAlternatives = new HashMap<>();
        this.currentSelections = new HashMap<>();

        // Inicializar alternativas y selecciones
        initializeAlternatives();
        initializeSelections();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_customize_outfit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OutfitCategory category = OutfitCategory.values()[position];

        // Configurar el título de la categoría
        holder.tvCategory.setText(getCategoryTitle(category));

        // Configurar el adaptador del spinner con las alternativas
        List<String> options = categoryAlternatives.get(category);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerOptions.setAdapter(adapter);

        // Establecer la selección actual
        String currentSelection = currentSelections.get(category);
        if (currentSelection != null) {
            int selectionIndex = options.indexOf(currentSelection);
            if (selectionIndex >= 0) {
                holder.spinnerOptions.setSelection(selectionIndex);
            }
        }

        // Escuchar cambios en la selección
        holder.spinnerOptions.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                String selected = (String) parent.getSelectedItem();
                currentSelections.put(category, selected);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // No hacer nada
            }
        });
    }

    @Override
    public int getItemCount() {
        return OutfitCategory.values().length;
    }

    // Método para obtener el outfit personalizado basado en las selecciones actuales
    public OutfitRecommendation getCustomizedOutfit() {
        List<String> tops = new ArrayList<>();
        tops.add(currentSelections.get(OutfitCategory.TOP));

        List<String> bottoms = new ArrayList<>();
        bottoms.add(currentSelections.get(OutfitCategory.BOTTOM));

        List<String> footwear = new ArrayList<>();
        footwear.add(currentSelections.get(OutfitCategory.FOOTWEAR));

        List<String> outerwear = new ArrayList<>();
        String outerwearItem = currentSelections.get(OutfitCategory.OUTERWEAR);
        if (outerwearItem != null && !outerwearItem.equals("-")) {
            outerwear.add(outerwearItem);
        }

        List<String> accessories = new ArrayList<>();
        String accessoriesItem = currentSelections.get(OutfitCategory.ACCESSORIES);
        if (accessoriesItem != null && !accessoriesItem.equals("-")) {
            // Dividir accesorios, ya que pueden ser múltiples separados por comas
            accessories.addAll(Arrays.asList(accessoriesItem.split(", ")));
        }

        return new OutfitRecommendation(
                tops, bottoms, footwear, outerwear, accessories, originalOutfit.getStyle());
    }

    // Inicializar las alternativas para cada categoría
    private void initializeAlternatives() {
        // Alternativas para prendas superiores
        List<String> topAlternatives = new ArrayList<>(originalOutfit.getTopItems());
        addDefaultAlternatives(topAlternatives, "Camiseta de algodón",
                "Camisa manga larga", "Camiseta de manga corta");
        categoryAlternatives.put(OutfitCategory.TOP, topAlternatives);

        // Alternativas para prendas inferiores
        List<String> bottomAlternatives = new ArrayList<>(originalOutfit.getBottomItems());
        addDefaultAlternatives(bottomAlternatives, "Jeans", "Pantalón de vestir",
                "Pantalón corto", "Falda");
        categoryAlternatives.put(OutfitCategory.BOTTOM, bottomAlternatives);

        // Alternativas para calzado
        List<String> footwearAlternatives = new ArrayList<>(originalOutfit.getFootwear());
        addDefaultAlternatives(footwearAlternatives, "Zapatillas", "Zapatos formales",
                "Sandalias", "Botas");
        categoryAlternatives.put(OutfitCategory.FOOTWEAR, footwearAlternatives);

        // Alternativas para prendas de abrigo
        List<String> outerwearAlternatives = new ArrayList<>(originalOutfit.getOuterWear());
        addDefaultAlternatives(outerwearAlternatives, "Chaqueta", "Abrigo",
                "Blazer", "Sudadera", "-");
        categoryAlternatives.put(OutfitCategory.OUTERWEAR, outerwearAlternatives);

        // Alternativas para accesorios
        List<String> accessoriesAlternatives = new ArrayList<>();
        if (!originalOutfit.getAccessories().isEmpty()) {
            // Unir accesorios en una sola string para el spinner
            accessoriesAlternatives.add(String.join(", ", originalOutfit.getAccessories()));
        }
        addDefaultAlternatives(accessoriesAlternatives, "Gafas de sol", "Bufanda",
                "Gorro", "Guantes", "-");
        categoryAlternatives.put(OutfitCategory.ACCESSORIES, accessoriesAlternatives);
    }

    // Añadir alternativas predeterminadas si no están ya en la lista
    private void addDefaultAlternatives(List<String> list, String... defaults) {
        for (String item : defaults) {
            if (!list.contains(item)) {
                list.add(item);
            }
        }
    }

    // Inicializar las selecciones actuales
    private void initializeSelections() {
        // Para prendas superiores
        if (!originalOutfit.getTopItems().isEmpty()) {
            currentSelections.put(OutfitCategory.TOP, originalOutfit.getTopItems().get(0));
        } else {
            currentSelections.put(OutfitCategory.TOP, categoryAlternatives.get(OutfitCategory.TOP).get(0));
        }

        // Para prendas inferiores
        if (!originalOutfit.getBottomItems().isEmpty()) {
            currentSelections.put(OutfitCategory.BOTTOM, originalOutfit.getBottomItems().get(0));
        } else {
            currentSelections.put(OutfitCategory.BOTTOM, categoryAlternatives.get(OutfitCategory.BOTTOM).get(0));
        }

        // Para calzado
        if (!originalOutfit.getFootwear().isEmpty()) {
            currentSelections.put(OutfitCategory.FOOTWEAR, originalOutfit.getFootwear().get(0));
        } else {
            currentSelections.put(OutfitCategory.FOOTWEAR, categoryAlternatives.get(OutfitCategory.FOOTWEAR).get(0));
        }

        // Para prendas de abrigo
        if (!originalOutfit.getOuterWear().isEmpty()) {
            currentSelections.put(OutfitCategory.OUTERWEAR, originalOutfit.getOuterWear().get(0));
        } else {
            currentSelections.put(OutfitCategory.OUTERWEAR, "-");
        }

        // Para accesorios
        if (!originalOutfit.getAccessories().isEmpty()) {
            currentSelections.put(OutfitCategory.ACCESSORIES, String.join(", ", originalOutfit.getAccessories()));
        } else {
            currentSelections.put(OutfitCategory.ACCESSORIES, "-");
        }
    }

    // Obtener título según la categoría
    private String getCategoryTitle(OutfitCategory category) {
        switch (category) {
            case TOP:
                return "Prenda Superior";
            case BOTTOM:
                return "Prenda Inferior";
            case FOOTWEAR:
                return "Calzado";
            case OUTERWEAR:
                return "Abrigo";
            case ACCESSORIES:
                return "Accesorios";
            default:
                return "";
        }
    }

    // ViewHolder para los elementos del RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory;
        Spinner spinnerOptions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            spinnerOptions = itemView.findViewById(R.id.spinnerOptions);
        }
    }
}
