package com.example.colormixingquiz.Model.Data;

import java.io.Serializable;

public class Question implements Serializable {
    private int id;
    private String firstColor;
    private String secondColor;
    private String correctMixColor;
    private boolean answered;

    // Constructor
    public Question(int id, String firstColor, String secondColor, String correctMixColor) {
        this.id = id;
        this.firstColor = firstColor;
        this.secondColor = secondColor;
        this.correctMixColor = correctMixColor;
        this.answered = false;
    }

    // Getters and setters
    public int getId() { return id; }
    public String getFirstColor() { return firstColor; }
    public String getSecondColor() { return secondColor; }
    public String getCorrectMixColor() { return correctMixColor; }
    public boolean isAnswered() { return answered; }
    public void setAnswered(boolean answered) { this.answered = answered; }
}