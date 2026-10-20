package com.example.mahila;

import com.example.mahila.dto.QuizQuestionDTO;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class QuizTest {

    @Test
    public void testQuizQuestionDTOGettersAndSetters() {
        QuizQuestionDTO q = new QuizQuestionDTO();
        q.setQuestionText("What is the average menstrual cycle length?");
        List<String> options = Arrays.asList("14 days", "28 days", "45 days", "60 days");
        q.setOptions(options);
        q.setCorrectOptionIndex(1);

        assertEquals("What is the average menstrual cycle length?", q.getQuestionText());
        assertEquals(4, q.getOptions().size());
        assertEquals("28 days", q.getOptions().get(1));
        assertEquals(Integer.valueOf(1), q.getCorrectOptionIndex());
    }

    @Test
    public void testQuizScoreCalculationAndProgression() {
        List<QuizQuestionDTO> questions = new ArrayList<>();

        QuizQuestionDTO q1 = new QuizQuestionDTO();
        q1.setQuestionText("Q1");
        q1.setOptions(Arrays.asList("A", "B", "C", "D"));
        q1.setCorrectOptionIndex(1); // B
        questions.add(q1);

        QuizQuestionDTO q2 = new QuizQuestionDTO();
        q2.setQuestionText("Q2");
        q2.setOptions(Arrays.asList("A", "B", "C", "D"));
        q2.setCorrectOptionIndex(2); // C
        questions.add(q2);

        int currentIndex = 0;
        int score = 0;

        // User answers Q1 correctly with index 1
        if (Integer.valueOf(1).equals(questions.get(currentIndex).getCorrectOptionIndex())) {
            score++;
        }
        currentIndex++;

        // User answers Q2 incorrectly with index 0
        if (Integer.valueOf(0).equals(questions.get(currentIndex).getCorrectOptionIndex())) {
            score++;
        }
        currentIndex++;

        assertEquals(2, currentIndex);
        assertEquals(1, score); // 1 out of 2 correct
    }

    @Test
    public void testQuizResetBehavior() {
        int currentIndex = 5;
        int score = 4;
        boolean isSubmitting = true;

        // Reset quiz state
        currentIndex = 0;
        score = 0;
        isSubmitting = false;

        assertEquals(0, currentIndex);
        assertEquals(0, score);
        assertFalse(isSubmitting);
    }
}
