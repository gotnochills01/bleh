package com.example.mahila;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mahila.api.ApiClient;
import com.example.mahila.api.ApiService;
import com.example.mahila.dto.QuizQuestionDTO;
import com.google.android.material.chip.ChipGroup;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private TextView tvQuestionText, tvCount, tvExplanation;
    private RadioGroup rgOptions;
    private RadioButton rbOpt0, rbOpt1, rbOpt2, rbOpt3;
    private ProgressBar pbQuiz;
    private ChipGroup cgCategoryFilters;
    private Button btnNext;

    private List<QuizQuestionDTO> questions = new ArrayList<>();
    private int currentIndex = 0;
    private int score = 0;
    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        tvQuestionText    = findViewById(R.id.tv_question_text);
        tvCount           = findViewById(R.id.tv_question_count);
        rgOptions         = findViewById(R.id.rg_options);
        rbOpt0            = findViewById(R.id.rb_opt0);
        rbOpt1            = findViewById(R.id.rb_opt1);
        rbOpt2            = findViewById(R.id.rb_opt2);
        rbOpt3            = findViewById(R.id.rb_opt3);
        pbQuiz            = findViewById(R.id.pb_quiz);

        btnNext = findViewById(R.id.btn_next_question);
        if (btnNext != null) {
            btnNext.setOnClickListener(v -> submitAnswer());
        }

        loadQuestionsForCategory("Menstrual Basics");
    }

    private void loadQuestionsForCategory(String category) {
        questions.clear();
        currentIndex = 0;
        score = 0;
        isSubmitting = false;

        ApiClient.getRequest(this, ApiService.QUIZZES + "?category=" + category, new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONArray arr = new JSONArray(result);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        QuizQuestionDTO q = new QuizQuestionDTO();
                        q.setQuestionText(obj.optString("questionText"));
                        q.setCorrectOptionIndex(obj.optInt("correctOptionIndex", 0));
                        JSONArray opts = obj.optJSONArray("options");
                        List<String> optList = new ArrayList<>();
                        if (opts != null) {
                            for (int j = 0; j < opts.length(); j++) {
                                optList.add(opts.getString(j));
                            }
                        }
                        q.setOptions(optList);
                        questions.add(q);
                    }
                    if (questions.isEmpty()) fallbackQuestions();
                    else displayQuestion();
                } catch (Exception e) {
                    fallbackQuestions();
                }
            }

            @Override
            public void onError(String message) {
                fallbackQuestions();
            }
        });
    }

    private void fallbackQuestions() {
        questions.clear();
        currentIndex = 0;
        score = 0;
        isSubmitting = false;

        // 10 Comprehensive Questions on Menstrual Health & Cycle Basics (WHO, ACOG, CDC guidelines)
        addQuestion("What is the typical average duration of a normal menstrual cycle?",
                Arrays.asList("14 days", "28 days (Normal range: 21–35 days)", "45 days", "60 days"), 1);

        addQuestion("Which hormone surge directly triggers ovulation during the cycle?",
                Arrays.asList("Estrogen", "Progesterone", "Luteinizing Hormone (LH)", "Cortisol"), 2);

        addQuestion("What does PCOS stand for in women's metabolic health?",
                Arrays.asList("Polycystic Ovary Syndrome", "Post Cycle Ovarian Surge", "Primary Cell Ovulation", "Poly Cellular State"), 0);

        addQuestion("What is Endometriosis?",
                Arrays.asList("A uterine skin infection", "Growth of tissue similar to uterine lining outside the uterus", "A harmless ovarian cyst", "A thyroid condition"), 1);

        addQuestion("How frequently should a disposable sanitary pad ideally be changed?",
                Arrays.asList("Every 12 hours", "Every 4 to 6 hours", "Once a day", "Only when fully soaked"), 1);

        addQuestion("Which dietary nutrient is essential to replenish after heavy period blood loss?",
                Arrays.asList("Calcium", "Vitamin D", "Iron", "Potassium"), 2);

        addQuestion("True or Fact: Is light to moderate exercise safe during menstruation?",
                Arrays.asList("Unsafe: increases cramps", "Fact: Gentle exercise releases endorphins and reduces dysmenorrhea", "Only safe in evening", "Completely forbidden"), 1);

        addQuestion("What is primary dysmenorrhea?",
                Arrays.asList("Recurrent pain during periods without underlying pelvic disease", "An ovarian tumor", "Inability to menstruate", "Excessive bleeding"), 0);

        addQuestion("Which phase of the cycle immediately follows ovulation?",
                Arrays.asList("Follicular phase", "Luteal phase", "Menstrual phase", "Pre-ovulatory phase"), 1);

        addQuestion("When should severe period pain (dysmenorrhea) be evaluated by a certified gynecologist?",
                Arrays.asList("Never, pain is always normal", "When it interferes with daily activities or causes fainting/nausea", "Only after age 40", "Only during menopause"), 1);

        Collections.shuffle(questions);
        displayQuestion();
    }

    private void addQuestion(String text, List<String> options, int correctIndex) {
        QuizQuestionDTO q = new QuizQuestionDTO();
        q.setQuestionText(text);
        q.setOptions(options);
        q.setCorrectOptionIndex(correctIndex);
        questions.add(q);
    }

    private void displayQuestion() {
        isSubmitting = false;
        if (btnNext != null) btnNext.setEnabled(true);

        if (currentIndex >= questions.size()) {
            showScoreSummary();
            return;
        }

        QuizQuestionDTO q = questions.get(currentIndex);
        if (tvCount != null) tvCount.setText("Question " + (currentIndex + 1) + " of " + questions.size());
        if (pbQuiz != null) {
            pbQuiz.setMax(questions.size());
            pbQuiz.setProgress(currentIndex + 1);
        }

        if (tvQuestionText != null) tvQuestionText.setText(q.getQuestionText());
        List<String> opts = q.getOptions();
        if (opts.size() > 0 && rbOpt0 != null) rbOpt0.setText(opts.get(0));
        if (opts.size() > 1 && rbOpt1 != null) rbOpt1.setText(opts.get(1));
        if (opts.size() > 2 && rbOpt2 != null) rbOpt2.setText(opts.get(2));
        if (opts.size() > 3 && rbOpt3 != null) rbOpt3.setText(opts.get(3));

        if (rgOptions != null) rgOptions.clearCheck();
    }

    private void submitAnswer() {
        if (isSubmitting) return;

        if (rgOptions == null) return;
        int checkedId = rgOptions.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, "Please select an answer!", Toast.LENGTH_SHORT).show();
            return;
        }

        isSubmitting = true;
        if (btnNext != null) btnNext.setEnabled(false);

        int selectedIdx = -1;
        if (checkedId == R.id.rb_opt0) selectedIdx = 0;
        else if (checkedId == R.id.rb_opt1) selectedIdx = 1;
        else if (checkedId == R.id.rb_opt2) selectedIdx = 2;
        else if (checkedId == R.id.rb_opt3) selectedIdx = 3;

        QuizQuestionDTO q = questions.get(currentIndex);
        if (selectedIdx == q.getCorrectOptionIndex()) {
            score++;
            Toast.makeText(this, "Correct! 🎉", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Incorrect. Correct answer: " + q.getOptions().get(q.getCorrectOptionIndex()), Toast.LENGTH_SHORT).show();
        }

        currentIndex++;
        new Handler(Looper.getMainLooper()).postDelayed(this::displayQuestion, 300);
    }

    private void showScoreSummary() {
        new AlertDialog.Builder(this)
                .setTitle("Quiz Completed! 🎉")
                .setMessage("Your Score: " + score + " / " + questions.size() + "\n\nDisclaimer: This quiz is for educational purposes based on WHO/ACOG guidelines and does not constitute a medical diagnosis.")
                .setPositiveButton("Retry Quiz", (dialog, which) -> {
                    fallbackQuestions();
                })
                .setNegativeButton("Close", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
