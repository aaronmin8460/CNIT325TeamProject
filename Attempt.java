/**
 * This class stores one student attempt on a question.
 */
public class Attempt {

    private int attemptId;

    private Student student;

    private Question question;

    private String submittedAnswer;

    private boolean correct;

    private int pointsEarned;

    public Attempt() {

        this(0, null, null, "", false, 0);

    }

    public Attempt(int attemptId, Student student, Question question, String submittedAnswer, boolean correct, int pointsEarned) {

        this.attemptId = attemptId;

        this.student = student;

        this.question = question;

        this.submittedAnswer = submittedAnswer;

        this.correct = correct;

        this.pointsEarned = pointsEarned;

    }

    public int getAttemptId() { return attemptId; }

    public void setAttemptId(int attemptId) { this.attemptId = attemptId; }

    public Student getStudent() { return student; }

    public void setStudent(Student student) { this.student = student; }

    public Question getQuestion() { return question; }

    public void setQuestion(Question question) { this.question = question; }

    public String getSubmittedAnswer() { return submittedAnswer; }

    public void setSubmittedAnswer(String submittedAnswer) { this.submittedAnswer = submittedAnswer; }

    public boolean isCorrect() { return correct; }

    public void setCorrect(boolean correct) { this.correct = correct; }

    public int getPointsEarned() { return pointsEarned; }

    public void setPointsEarned(int pointsEarned) { this.pointsEarned = pointsEarned; }

}
