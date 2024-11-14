package com.example.colormixingquiz.Controller;

import com.example.colormixingquiz.Model.Data.Question;
import com.example.colormixingquiz.Model.Repository.QuestionRepository;
import com.example.colormixingquiz.View.Fragment.GamePreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GameController {
    private QuestionRepository repository;
    private List<Question> currentGameQuestions;
    private int currentQuestionIndex;
    private int correctAnswers;
    private boolean gameInProgress;

    public GameController() {
        repository = QuestionRepository.getInstance();
        currentGameQuestions = new ArrayList<>();
        currentQuestionIndex = 0;
        correctAnswers = 0;
        gameInProgress = false;
    }

    public void startNewGame(boolean onlyUnanswered) {
        if (repository == null) {
            repository = QuestionRepository.getInstance();
        }

        if (onlyUnanswered) {
            currentGameQuestions = new ArrayList<>(repository.getUnansweredQuestions());
        } else {
            currentGameQuestions = new ArrayList<>(repository.getAllQuestions());
        }

        // Validate that we have questions to play with
        if (currentGameQuestions.isEmpty()) {
            throw new IllegalStateException("No questions available to start the game");
        }

        Collections.shuffle(currentGameQuestions);
        currentQuestionIndex = 0;
        correctAnswers = 0;
        gameInProgress = true;
    }

    public void resumeGame(GamePreferences.GameProgress progress) {
        if (repository == null) {
            repository = QuestionRepository.getInstance();
        }

        // Load the full question set
        currentGameQuestions = new ArrayList<>(repository.getAllQuestions());

        // Validate the progress
        if (progress == null || currentGameQuestions.isEmpty()) {
            throw new IllegalStateException("Invalid game progress or no questions available");
        }

        // Ensure the current question index is valid
        currentQuestionIndex = Math.min(progress.currentQuestion, currentGameQuestions.size() - 1);
        correctAnswers = progress.correctAnswers;
        gameInProgress = true;
    }

    public Question getCurrentQuestion() {
        if (currentGameQuestions == null || currentGameQuestions.isEmpty() ||
                currentQuestionIndex >= currentGameQuestions.size()) {
            return null;
        }
        return currentGameQuestions.get(currentQuestionIndex);
    }

    public List<String> getRandomizedAnswers() {
        Question current = getCurrentQuestion();
        if (current == null) return new ArrayList<>();

        // Get the correct answer
        List<String> answers = new ArrayList<>();
        answers.add(current.getCorrectMixColor());

        // Get all possible wrong answers (other questions' correct answers)
        List<String> otherAnswers = repository.getAllQuestions().stream()
                .filter(q -> q.getId() != current.getId())
                .map(Question::getCorrectMixColor)
                .distinct()
                .collect(Collectors.toList());

        // Make sure we have enough wrong answers
        if (otherAnswers.size() < 3) {
            // Add some default wrong answers if we don't have enough
            List<String> defaultWrongAnswers = Arrays.asList("Brown", "Gray", "Pink");
            for (String answer : defaultWrongAnswers) {
                if (!answers.contains(answer) && !otherAnswers.contains(answer)) {
                    otherAnswers.add(answer);
                }
            }
        }

        // Shuffle and take 3 wrong answers
        Collections.shuffle(otherAnswers);
        for (int i = 0; i < 3 && i < otherAnswers.size(); i++) {
            answers.add(otherAnswers.get(i));
        }

        // Shuffle all answers
        Collections.shuffle(answers);
        return answers;
    }

    public boolean checkAnswer(String selectedAnswer) {
        Question current = getCurrentQuestion();
        if (current == null) return false;

        boolean isCorrect = current.getCorrectMixColor().equals(selectedAnswer);
        if (isCorrect) {
            current.setAnswered(true);
            correctAnswers++;
        }
        return isCorrect;
    }

    public void nextQuestion() {
        if (currentQuestionIndex < currentGameQuestions.size()) {
            currentQuestionIndex++;
        }
    }

    public boolean isGameFinished() {
        return currentQuestionIndex >= currentGameQuestions.size();
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public int getTotalQuestions() {
        return currentGameQuestions.size();
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public boolean hasUnansweredQuestions() {
        return !repository.getUnansweredQuestions().isEmpty();
    }

    public boolean isGameInProgress() {
        return gameInProgress;
    }

    public void endGame() {
        gameInProgress = false;
    }

    public void resetAllAnswers() {
        repository.resetAllAnswers();
    }

    // Helper method to get statistics for the history page
    public GameStatistics getGameStatistics() {
        List<Question> allQuestions = repository.getAllQuestions();
        int totalQuestions = allQuestions.size();
        int answeredQuestions = (int) allQuestions.stream()
                .filter(Question::isAnswered)
                .count();

        return new GameStatistics(answeredQuestions, totalQuestions);
    }

    // Static inner class for game statistics
    public static class GameStatistics {
        private final int answeredQuestions;
        private final int totalQuestions;

        public GameStatistics(int answeredQuestions, int totalQuestions) {
            this.answeredQuestions = answeredQuestions;
            this.totalQuestions = totalQuestions;
        }

        public int getAnsweredQuestions() {
            return answeredQuestions;
        }

        public int getTotalQuestions() {
            return totalQuestions;
        }

        public float getCompletionPercentage() {
            return totalQuestions > 0 ?
                    (answeredQuestions * 100f / totalQuestions) : 0f;
        }
    }
}