package com.example.mahila;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mahila.dto.NutritionDTO;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class NutritionAdapter extends RecyclerView.Adapter<NutritionAdapter.NutritionViewHolder> {

    private final Context context;
    private final List<NutritionDTO> items;

    public NutritionAdapter(Context context, List<NutritionDTO> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public NutritionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_nutrition_recommendation, parent, false);
        return new NutritionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NutritionViewHolder holder, int position) {
        NutritionDTO dto = items.get(position);
        holder.tvFoodName.setText(dto.getFoodItemName());
        holder.tvMealCategory.setText(dto.getMealCategory());
        holder.tvKeyNutrients.setText("Key Nutrients: " + dto.getKeyNutrients());
        holder.tvHealthBenefits.setText(dto.getHealthBenefits());
        holder.tvEvidenceLevel.setText("Evidence: " + dto.getEvidenceLevel());

        if (dto.getSafetyWarnings() != null && !dto.getSafetyWarnings().trim().isEmpty()) {
            holder.tvSafetyWarning.setText("Precaution: " + dto.getSafetyWarnings());
            holder.tvSafetyWarning.setVisibility(View.VISIBLE);
        } else {
            holder.tvSafetyWarning.setVisibility(View.GONE);
        }

        if (dto.getEvidenceUrl() != null && dto.getEvidenceUrl().startsWith("http")) {
            holder.btnEvidenceLink.setVisibility(View.VISIBLE);
            holder.btnEvidenceLink.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(dto.getEvidenceUrl()));
                try {
                    context.startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(context, "Unable to open browser", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            holder.btnEvidenceLink.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NutritionViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvMealCategory, tvKeyNutrients, tvHealthBenefits, tvEvidenceLevel, tvSafetyWarning;
        MaterialButton btnEvidenceLink;

        public NutritionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tv_food_name);
            tvMealCategory = itemView.findViewById(R.id.tv_meal_category);
            tvKeyNutrients = itemView.findViewById(R.id.tv_key_nutrients);
            tvHealthBenefits = itemView.findViewById(R.id.tv_health_benefits);
            tvEvidenceLevel = itemView.findViewById(R.id.tv_evidence_level);
            tvSafetyWarning = itemView.findViewById(R.id.tv_safety_warning);
            btnEvidenceLink = itemView.findViewById(R.id.btn_evidence_link);
        }
    }
}
