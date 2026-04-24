package model;

/**
 * This class stores a short answer question.
 */
public class ShortAnswerQuestion extends Question {

    private String sampleAnswer;

    public ShortAnswerQuestion() {

        this(0, "", 0, "");

    }

    public ShortAnswerQuestion(int questionId, String prompt, int points, String classCode) {

        super(questionId, prompt, points, classCode);

        this.sampleAnswer = "";

    }

    public String getSampleAnswer() { return sampleAnswer; }

    public void setSampleAnswer(String sampleAnswer) { this.sampleAnswer = sampleAnswer; }

}
