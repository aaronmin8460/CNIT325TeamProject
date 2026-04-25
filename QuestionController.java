import java.util.*;

/**
 * This class stores question information for the server.
 */
public class QuestionController {

    private DataService dataService;

    private HashMap<String, Question> questionsById;

    public QuestionController() {

        this(null);

    }

    public QuestionController(DataService dataService) {

        this.dataService = dataService;

        this.questionsById = new HashMap<String, Question>();

    }

    public Question saveQuestion(String classCode, Question question) {

        Question savedQuestion;

        if (dataService == null) {
            return null;
        }

        savedQuestion = dataService.saveQuestion(classCode, question);

        if (savedQuestion != null) {
            if (savedQuestion.getQuestionId() != null && savedQuestion.getQuestionId().length() > 0) {
                questionsById.put(savedQuestion.getQuestionId(), savedQuestion);
            }
        }

        return savedQuestion;

    }

    public Question findQuestionById(String questionId) {

        if (questionId == null) {
            return null;
        }

        return questionsById.get(questionId);

    }

    public ArrayList<Question> getQuestionsForClass(String classCode) {

        if (dataService == null) {
            return new ArrayList<Question>();
        }

        return dataService.getQuestionsForClass(classCode);

    }

    public boolean isCorrectAnswer(Question question, String answer) {

        String correctAnswer;
        String studentAnswer;

        correctAnswer = getCorrectAnswer(question);
        studentAnswer = cleanText(answer);

        return correctAnswer.equalsIgnoreCase(studentAnswer);

    }

    public String getCorrectAnswer(Question question) {

        if (question instanceof MultipleChoiceQuestion) {
            return cleanText(((MultipleChoiceQuestion) question).getCorrectAnswer());
        }

        if (question instanceof TrueFalseQuestion) {
            return String.valueOf(((TrueFalseQuestion) question).isCorrectAnswer());
        }

        if (question instanceof ShortAnswerQuestion) {
            return cleanText(((ShortAnswerQuestion) question).getSampleAnswer());
        }

        return "";

    }

    public String getQuestionType(Question question) {

        if (question instanceof MultipleChoiceQuestion) {
            return "MULTIPLE_CHOICE";
        }

        if (question instanceof TrueFalseQuestion) {
            return "TRUE_FALSE";
        }

        if (question instanceof ShortAnswerQuestion) {
            return "SHORT_ANSWER";
        }

        return "QUESTION";

    }

    public String getChoiceText(Question question, int index) {

        ArrayList<String> choices;

        if (question instanceof MultipleChoiceQuestion) {
            choices = ((MultipleChoiceQuestion) question).getChoices();

            if (choices != null) {
                if (index >= 0 && index < choices.size()) {
                    return cleanText(choices.get(index));
                }
            }
        }

        if (question instanceof TrueFalseQuestion) {
            if (index == 0) {
                return "True";
            }

            if (index == 1) {
                return "False";
            }
        }

        return "";

    }

    private String cleanText(String text) {

        String safeText;

        if (text == null) {
            return "";
        }

        safeText = text;
        safeText = safeText.replace("|", "/");
        safeText = safeText.replace("\n", " ");
        safeText = safeText.replace("\r", " ");

        return safeText;

    }

    public DataService getDataService() {
        return dataService;
    }

    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }

    public HashMap<String, Question> getQuestionsById() {
        return questionsById;
    }

    public void setQuestionsById(HashMap<String, Question> questionsById) {
        this.questionsById = questionsById;
    }

}
