package model;

/**
 * This class stores a true or false question.
 */
public class TrueFalseQuestion extends Question {

    private boolean correctAnswer;

    public TrueFalseQuestion() {

        this(0, "", 0, "");

    }

    public TrueFalseQuestion(int questionId, String prompt, int points, String classCode) {

        super(questionId, prompt, points, classCode);

        this.correctAnswer = false;

    }

    public boolean isCorrectAnswer() { return correctAnswer; }

    public void setCorrectAnswer(boolean correctAnswer) { this.correctAnswer = correctAnswer; }

}
