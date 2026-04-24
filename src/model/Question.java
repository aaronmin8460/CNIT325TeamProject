package model;

/**
 * This class is the abstract parent class for all question types.
 */
public abstract class Question {

    private int questionId;

    private String prompt;

    private int points;

    private String classCode;

    public Question() {

        this(0, "", 0, "");

    }

    public Question(int questionId, String prompt, int points, String classCode) {

        this.questionId = questionId;

        this.prompt = prompt;

        this.points = points;

        this.classCode = classCode;

    }

    public int getQuestionId() { return questionId; }

    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getPrompt() { return prompt; }

    public void setPrompt(String prompt) { this.prompt = prompt; }

    public int getPoints() { return points; }

    public void setPoints(int points) { this.points = points; }

    public String getClassCode() { return classCode; }

    public void setClassCode(String classCode) { this.classCode = classCode; }

}
