package com.example.mahila;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mahila.api.ApiClient;
import com.example.mahila.api.ApiService;
import com.example.mahila.api.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.Locale;

public class AiChatActivity extends BaseActivity {

    private LinearLayout containerChat;
    private EditText etPrompt;
    private ScrollView scrollChat;
    private MaterialButton btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        containerChat = findViewById(R.id.container_chat_messages);
        etPrompt = findViewById(R.id.et_ai_prompt);
        scrollChat = findViewById(R.id.scroll_chat);
        btnSend = findViewById(R.id.btn_send_prompt);

        if (btnSend != null) {
            btnSend.setOnClickListener(v -> sendMessage());
        }

        addMessage("Hello! I am your Mahila AI Health Assistant. Ask me anything about menstrual health, PCOS, symptoms, nutrition, or wellness.", false);
    }

    private void setLoadingState(boolean isLoading) {
        if (btnSend != null) {
            btnSend.setEnabled(!isLoading);
            btnSend.setText(isLoading ? getString(R.string.thinking) : getString(R.string.send));
        }
        if (etPrompt != null) {
            etPrompt.setEnabled(!isLoading);
        }
    }

    private void sendMessage() {
        if (etPrompt == null) return;
        String query = etPrompt.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_enter_prompt), Toast.LENGTH_SHORT).show();
            return;
        }

        addMessage(query, true);
        etPrompt.setText("");
        setLoadingState(true);

        try {
            TokenManager tokenManager = new TokenManager(this);
            JSONObject req = new JSONObject();
            req.put("prompt", query);
            req.put("category", "GENERAL");
            req.put("language", tokenManager.getLanguage());

            ApiClient.postRequest(this, ApiService.AI_CHAT, req, new ApiClient.ApiCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    setLoadingState(false);
                    try {
                        JSONObject res = new JSONObject(result);
                        String answer = res.optString("response", "");
                        String disclaimer = res.optString("disclaimer", "");
                        if (answer == null || answer.trim().isEmpty()) {
                            throw new IllegalArgumentException("Empty response");
                        }

                        String displayText = answer;
                        if (!disclaimer.isEmpty()) {
                            displayText = displayText + "\n\n" + disclaimer;
                        }
                        addMessage(displayText, false);
                    } catch (Exception e) {
                        generateLocalAiResponse(query, true);
                    }
                }

                @Override
                public void onError(String message) {
                    setLoadingState(false);
                    String contextualMessage = message == null || message.trim().isEmpty()
                            ? getString(R.string.ai_fallback_notice)
                            : getString(R.string.ai_fallback_notice) + "\n\n" + message;
                    addMessage(contextualMessage, false);
                }
            });
        } catch (Exception e) {
            setLoadingState(false);
            addMessage(getString(R.string.ai_fallback_notice), false);
        }
    }

    public static String getEducationalFallbackForPrompt(String query) {
        if (query == null) return "Cycle tracking, hydration, and restful sleep support overall reproductive health.";
        String prompt = query.toLowerCase();
        if (prompt.contains("pcos") || prompt.contains("ovary")) {
            return "Polycystic Ovary Syndrome (PCOS) is a hormonal condition. Regular moderate exercise (30 mins/day), a low-GI balanced diet, and stress management help regulate symptoms. Consult your doctor for personal guidance.";
        } else if (prompt.contains("cramp") || prompt.contains("pain")) {
            return "To ease menstrual cramps: apply a warm heating pad, drink warm chamomile or ginger tea, and practice gentle stretching. Consult a doctor if pain is severe.";
        } else if (prompt.contains("diet") || prompt.contains("food") || prompt.contains("nutrition")) {
            return "Incorporate iron-rich foods (spinach, lentils) and Vitamin C (citrus fruits) during your period to support healthy blood iron levels.";
        } else if (prompt.contains("ovulation") || prompt.contains("fertile")) {
            return "Ovulation generally occurs around 14 days before your next cycle start. Stretchy cervical mucus and a slight body temperature rise are common signs.";
        } else {
            return "Maintaining a balanced routine with hydration, nutrient-dense foods, 7-9 hours of sleep, and cycle tracking fosters long-term health.";
        }
    }

    private void generateLocalAiResponse(String query, boolean indicateFallback) {
        String answer = getEducationalFallbackForPrompt(query);
        if (indicateFallback) {
            answer = getString(R.string.ai_fallback_notice) + "\n\n" + answer;
        }
        addMessage(answer, false);
    }

    private void addMessage(String text, boolean isUser) {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(16f);
        card.setCardElevation(1f);
        card.setContentPadding(24, 16, 24, 16);

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14f);
        tv.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(this, R.font.nunito));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);

        if (isUser) {
            params.gravity = android.view.Gravity.END;
            card.setCardBackgroundColor(0xFFF3E8EC); // Deep Plum light container
            tv.setTextColor(0xFF3B1F2B);
        } else {
            params.gravity = android.view.Gravity.START;
            card.setCardBackgroundColor(0xFFE4EFE6); // Sage Green light container
            tv.setTextColor(0xFF253D2A);
        }
        card.setLayoutParams(params);
        card.addView(tv);

        containerChat.addView(card);
        scrollChat.post(() -> scrollChat.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
