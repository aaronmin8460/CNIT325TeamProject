package model;

import java.util.ArrayList;

/**
 * This class stores a multiple-choice question.
 */
public class MultipleChoiceQuestion extends Question {

    private ArrayList<String> choices;

    private String correctAnswer;

    public MultipleChoiceQuestion() {

        this(0, "", 0, "");

    }

    public MultipleChoiceQuestion(int questionId, String prompt, int points, String classCode) {

        super(questionId, prompt, points, classCode);

        this.choices = new ArrayList<String>();

        this.correctAnswer = "";

    }

    public ArrayList<String> getChoices() { return choices; }

    public void setChoices(ArrayList<String> choices) { this.choices = choices; }

    public String getCorrectAnswer() { return correctAnswer; }

    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

}
