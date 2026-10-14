package com.example.mahila.ui.awareness;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.mahila.QuizActivity;
import com.example.mahila.R;

public class AwarenessFragment extends Fragment {

    private static final String[][] CATEGORIES = new String[][]{
            {"Menstrual Basics & Cycle Education", "Learn about follicular, ovulatory, and luteal phases, flow variance, and tracking."},
            {"Adolescent & First Period Guidance", "Reassuring guidance for young teenagers navigating early menarche and body changes."},
            {"Reproductive Health (20s & 30s)", "Fertility window tracking, ovulation signs, and pre-conception wellness."},
            {"Perimenopause & Menopause", "Understanding hormonal shifts, hot flashes, bone density, and wellness past 40."},
            {"PCOS / PCOD & Endometriosis Awareness", "Educational insights on insulin sensitivity, pelvic health, and symptom management."},
            {"Menstrual Hygiene & Safety", "Safe product usage, pad/cup hygiene, and preventing infections (UTI/BV)."}
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_awareness, container, false);

        Button btnStartQuiz = root.findViewById(R.id.btn_start_quiz);
        if (btnStartQuiz != null) {
            btnStartQuiz.setOnClickListener(v -> startActivity(new Intent(requireContext(), QuizActivity.class)));
        }

        LinearLayout containerCategories = root.findViewById(R.id.container_categories);
        if (containerCategories != null) {
            containerCategories.removeAllViews();
            for (String[] cat : CATEGORIES) {
                CardView card = new CardView(requireContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 16);
                card.setLayoutParams(params);
                card.setRadius(20f);
                card.setCardElevation(2f);
                card.setContentPadding(24, 24, 24, 24);

                LinearLayout itemLayout = new LinearLayout(requireContext());
                itemLayout.setOrientation(LinearLayout.VERTICAL);

                TextView tvTitle = new TextView(requireContext());
                tvTitle.setText("📖  " + cat[0]);
                tvTitle.setTextSize(16f);
                tvTitle.setTextColor(0xFF3B1F2B);
                tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

                TextView tvDesc = new TextView(requireContext());
                tvDesc.setText(cat[1]);
                tvDesc.setTextSize(13f);
                tvDesc.setTextColor(0xFF6E5A63);
                tvDesc.setPadding(0, 8, 0, 0);

                itemLayout.addView(tvTitle);
                itemLayout.addView(tvDesc);
                card.addView(itemLayout);

                containerCategories.addView(card);
            }

            // Medical Disclaimer Banner
            CardView disclaimerCard = new CardView(requireContext());
            LinearLayout.LayoutParams dParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            dParams.setMargins(0, 16, 0, 16);
            disclaimerCard.setLayoutParams(dParams);
            disclaimerCard.setRadius(16f);
            disclaimerCard.setCardElevation(0f);
            disclaimerCard.setCardBackgroundColor(0xFFF5EBE6);
            disclaimerCard.setContentPadding(20, 20, 20, 20);

            TextView tvDisclaimer = new TextView(requireContext());
            tvDisclaimer.setText("⚠️ Medical Disclaimer:\nContent provided is for educational and informational purposes only and does not constitute medical advice or diagnosis. Always consult a certified gynecologist or medical professional for health concerns.");
            tvDisclaimer.setTextSize(12f);
            tvDisclaimer.setTextColor(0xFF5E1E34);

            disclaimerCard.addView(tvDisclaimer);
            containerCategories.addView(disclaimerCard);
        }

        return root;
    }
}
