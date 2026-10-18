package com.example.mahila;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.net.URLEncoder;

public class AwarenessDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String EXTRA_READ_TIME = "EXTRA_READ_TIME";
    public static final String EXTRA_CONTENT = "EXTRA_CONTENT";
    public static final String EXTRA_SOURCE = "EXTRA_SOURCE";
    public static final String EXTRA_TAG = "EXTRA_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_awareness_detail);

        View root = findViewById(R.id.awareness_detail_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        MaterialToolbar toolbar = findViewById(R.id.awareness_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Health Topic Details");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        Intent intent = getIntent();
        String title = intent.getStringExtra(EXTRA_TITLE);
        String readTime = intent.getStringExtra(EXTRA_READ_TIME);
        String content = intent.getStringExtra(EXTRA_CONTENT);
        String source = intent.getStringExtra(EXTRA_SOURCE);
        String tag = intent.getStringExtra(EXTRA_TAG);

        if (title == null) title = "Menstrual Health & Cycle Care";
        if (readTime == null) readTime = "3 min read";
        if (source == null) source = "ACOG & WHO Clinical Guidelines";

        TextView tvTitle = findViewById(R.id.tv_detail_title);
        TextView tvSubtitle = findViewById(R.id.tv_detail_subtitle);
        TextView tvOverview = findViewById(R.id.tv_detail_overview);
        TextView tvScenario = findViewById(R.id.tv_detail_scenario);
        LinearLayout containerResearch = findViewById(R.id.container_research_links);

        if (tvTitle != null) tvTitle.setText(title);
        if (tvSubtitle != null) tvSubtitle.setText(readTime + "  •  " + source);
        if (tvOverview != null && content != null) tvOverview.setText(content);

        if (tvScenario != null) {
            if ("pcos".equalsIgnoreCase(tag)) {
                tvScenario.setText("Priya (age 24) presented with oligomenorrhea (45-day cycles) and severe pelvic discomfort. Ultrasound & hormonal panel confirmed mild PCOS with insulin resistance. Lifestyle modifications and targeted metabolic care restored 30-day regular cycle flow.");
            } else if ("hygiene".equalsIgnoreCase(tag)) {
                tvScenario.setText("Ananya (age 19) noted recurrent vaginal itching when using scented hygiene products during college exams. Discontinuing scented wash and adopting 100% breathable cotton pads resolved the inflammation completely.");
            } else if ("myths".equalsIgnoreCase(tag)) {
                tvScenario.setText("Kavya (age 21) avoided all physical activity during periods believing it aggravated blood loss. Incorporating 20-minute daily walking reduced menstrual cramps (dysmenorrhea) by 40% without increasing flow volume.");
            } else {
                tvScenario.setText("Riya (age 26) tracked cycle length variance for 6 months. Noticing cycle lengths ranging from 26 to 29 days gave her confidence during reproductive health consultations.");
            }
        }

        renderResearchLinks(containerResearch, title, tag);
    }

    private void renderResearchLinks(LinearLayout container, String topicQuery, String tag) {
        if (container == null) return;
        container.removeAllViews();

        addResearchButton(container, "📚  PubMed Research (NCBI Scientific Studies)",
                "https://pubmed.ncbi.nlm.nih.gov/?term=" + encodeUrl(topicQuery));

        addResearchButton(container, "🎓  Google Scholar (Peer-Reviewed Articles)",
                "https://scholar.google.com/scholar?q=" + encodeUrl(topicQuery));

        addResearchButton(container, "🏥  MedlinePlus Health Library",
                "https://medlineplus.gov/search?q=" + encodeUrl(topicQuery));

        addResearchButton(container, "🌐  Wikipedia Background Reading",
                "https://en.wikipedia.org/wiki/Special:Search?search=" + encodeUrl(topicQuery));

        addResearchButton(container, "🔍  Google Search (Broader Resources)",
                "https://www.google.com/search?q=" + encodeUrl(topicQuery + " women's health medical guidance"));
    }

    private void addResearchButton(LinearLayout container, String label, String targetUrl) {
        MaterialButton button = new MaterialButton(new ContextThemeWrapper(this, com.google.android.material.R.style.Widget_Material3_Button_TonalButton));
        button.setText(label);
        button.setTextSize(13f);
        button.setCornerRadius(14);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 12);
        button.setLayoutParams(params);
        button.setOnClickListener(v -> openExternalUrl(targetUrl));
        container.addView(button);
    }

    private void openExternalUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No web browser available to open link", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not open URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String encodeUrl(String query) {
        try {
            return URLEncoder.encode(query, "UTF-8");
        } catch (Exception e) {
            return query;
        }
    }
}
