package com.example.colormixingquiz.Model.Repository;

import com.example.colormixingquiz.Model.Data.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestionRepository {
    private List<Question> questions;
    private static QuestionRepository instance;

    private QuestionRepository() {
        questions = new ArrayList<>();
        initializeQuestions();
    }

    public static QuestionRepository getInstance() {
        if (instance == null) {
            instance = new QuestionRepository();
        }
        return instance;
    }

    private void initializeQuestions() {
        // Add sample questions
        questions.add(new Question(1, "Red", "Blue", "Purple"));
        questions.add(new Question(2, "Yellow", "Blue", "Green"));
        questions.add(new Question(3, "Red", "Yellow", "Orange"));
        // Add more questions as needed
    }

    public List<Question> getAllQuestions() {
        return new ArrayList<>(questions);
    }

    public List<Question> getUnansweredQuestions() {
        return questions.stream()
                .filter(q -> !q.isAnswered())
                .collect(Collectors.toList());
    }

    public void resetAllAnswers() {
        questions.forEach(q -> q.setAnswered(false));
    }
}