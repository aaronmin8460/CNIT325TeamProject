import java.util.*;

/**
 * This interface defines simple data operations for the project.
 */
public interface DataService {

    User login(String email, String password);

    CourseClass createClass(String className, Instructor instructor);

    boolean joinClass(String classCode, Student student);

    CourseClass findClassByCode(String classCode);

    Question saveQuestion(String classCode, Question question);

    ArrayList<Question> getQuestionsForClass(String classCode);

    Attempt saveAttempt(Attempt attempt);

    ArrayList<Attempt> getAttemptsForClass(String classCode);

}
