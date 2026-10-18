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

import com.example.mahila.AwarenessDetailActivity;
import com.example.mahila.QuizActivity;
import com.example.mahila.R;
import com.google.android.material.chip.ChipGroup;

public class AwarenessFragment extends Fragment {

    private static final String[][] ARTICLES = new String[][]{
            {"Menstrual Basics & Cycle Biology", "basics", "3 min read", "• Follicular Phase: Estrogen rises, building uterine lining.\n• Ovulatory Phase: Peak fertility window lasting ~24–36 hours.\n• Luteal Phase: Progesterone dominates before menarche.\n\nKey Takeaway: Understanding your cycle length variance helps differentiate regular fluctuations from hormonal imbalances.", "WHO & ACOG Evidence Guidelines"},
            {"PCOS & Endometriosis Symptom Care", "pcos", "5 min read", "• PCOS: Insulin resistance & androgen elevation causing irregular periods.\n• Endometriosis: Tissue similar to uterine lining growing outside uterus, causing chronic pelvic pain.\n\nIllustrative Scenario: Priya (age 24) experienced severe cramps and 45-day cycles. Consultation revealed mild PCOS; dietary changes and targeted care improved regularity.", "NIH Women's Health Research"},
            {"Menstrual Hygiene & Safety Standards", "hygiene", "4 min read", "• Change pads every 4–6 hours; menstrual cups every 8–12 hours.\n• Wash hands before and after product changes to prevent bacterial infection.\n• Avoid scented wash products that alter vaginal pH levels.", "CDC Reproductive Health Standards"},
            {"Myths vs Facts: Period Care", "myths", "3 min read", "• Myth: Exercise is unsafe during periods.\n• Fact: Gentle exercise reduces dysmenorrhea and releases endorphins.\n• Myth: Period pain is always normal.\n• Fact: Severe pain causing fainting or nausea requires medical evaluation.", "Johns Hopkins Medicine"}
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
        ChipGroup chipGroup = root.findViewById(R.id.cg_awareness_filters);

        renderArticles(containerCategories, "all");

        if (chipGroup != null) {
            chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
                String tag = "all";
                if (checkedId == R.id.chip_filter_basics) tag = "basics";
                else if (checkedId == R.id.chip_filter_pcos) tag = "pcos";
                else if (checkedId == R.id.chip_filter_hygiene) tag = "hygiene";
                else if (checkedId == R.id.chip_filter_myths) tag = "myths";
                renderArticles(containerCategories, tag);
            });
        }

        return root;
    }

    private void renderArticles(LinearLayout container, String filterTag) {
        if (container == null) return;
        container.removeAllViews();

        for (String[] article : ARTICLES) {
            String title = article[0];
            String tag = article[1];
            String readTime = article[2];
            String content = article[3];
            String source = article[4];

            if (!filterTag.equals("all") && !tag.equals(filterTag)) {
                continue;
            }

            CardView card = new CardView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 20);
            card.setLayoutParams(params);
            card.setRadius(20f);
            card.setCardElevation(2f);
            card.setContentPadding(24, 24, 24, 24);

            LinearLayout itemLayout = new LinearLayout(requireContext());
            itemLayout.setOrientation(LinearLayout.VERTICAL);

            TextView tvTitle = new TextView(requireContext());
            tvTitle.setText("📖  " + title + "  •  " + readTime);
            tvTitle.setTextSize(16f);
            tvTitle.setTextColor(0xFF3B1F2B);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvBody = new TextView(requireContext());
            tvBody.setText(content);
            tvBody.setTextSize(14f);
            tvBody.setTextColor(0xFF6E5A63);
            tvBody.setPadding(0, 10, 0, 10);
            tvBody.setLineSpacing(4f, 1f);

            TextView tvSource = new TextView(requireContext());
            tvSource.setText("Source: " + source + "  (Tap to open in-app page ➔)");
            tvSource.setTextSize(12f);
            tvSource.setTextColor(0xFF7DA183);
            tvSource.setTypeface(null, android.graphics.Typeface.BOLD);

            itemLayout.addView(tvTitle);
            itemLayout.addView(tvBody);
            itemLayout.addView(tvSource);
            card.addView(itemLayout);

            card.setOnClickListener(v -> {
                Intent detailIntent = new Intent(requireContext(), AwarenessDetailActivity.class);
                detailIntent.putExtra(AwarenessDetailActivity.EXTRA_TITLE, title);
                detailIntent.putExtra(AwarenessDetailActivity.EXTRA_READ_TIME, readTime);
                detailIntent.putExtra(AwarenessDetailActivity.EXTRA_CONTENT, content);
                detailIntent.putExtra(AwarenessDetailActivity.EXTRA_SOURCE, source);
                detailIntent.putExtra(AwarenessDetailActivity.EXTRA_TAG, tag);
                startActivity(detailIntent);
            });

            container.addView(card);
        }

        // Medical Disclaimer Banner
        CardView disclaimerCard = new CardView(requireContext());
        LinearLayout.LayoutParams dParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dParams.setMargins(0, 12, 0, 24);
        disclaimerCard.setLayoutParams(dParams);
        disclaimerCard.setRadius(16f);
        disclaimerCard.setCardElevation(0f);
        disclaimerCard.setCardBackgroundColor(0xFFF5EBE6);
        disclaimerCard.setContentPadding(20, 20, 20, 20);

        TextView tvDisclaimer = new TextView(requireContext());
        tvDisclaimer.setText("⚠️ Medical Disclaimer:\nContent provided is for educational purposes based on established clinical guidelines (ACOG, WHO, CDC). It does not constitute medical advice. Consult a certified gynecologist for personalized medical care.");
        tvDisclaimer.setTextSize(13f);
        tvDisclaimer.setTextColor(0xFF5E1E34);
        tvDisclaimer.setLineSpacing(3f, 1f);

        disclaimerCard.addView(tvDisclaimer);
        container.addView(disclaimerCard);
    }
}
