/**
 * This class is the abstract parent class for all question types.
 */
public abstract class Question {

    private String questionId;

    private String prompt;

    private int points;

    private String classCode;

    public Question() {

        this("", "", 0, "");

    }

    public Question(String questionId, String prompt, int points, String classCode) {

        this.questionId = questionId;

        this.prompt = prompt;

        this.points = points;

        this.classCode = classCode;

    }

    public Question(int questionId, String prompt, int points, String classCode) {

        this(String.valueOf(questionId), prompt, points, classCode);

    }

    public String getQuestionId() { return questionId; }

    public void setQuestionId(String questionId) {

        if (questionId == null) {
            this.questionId = "";
        } else {
            this.questionId = questionId;
        }

    }

    public void setQuestionId(int questionId) { this.questionId = String.valueOf(questionId); }

    public String getPrompt() { return prompt; }

    public void setPrompt(String prompt) { this.prompt = prompt; }

    public int getPoints() { return points; }

    public void setPoints(int points) { this.points = points; }

    public String getClassCode() { return classCode; }

    public void setClassCode(String classCode) { this.classCode = classCode; }

}
