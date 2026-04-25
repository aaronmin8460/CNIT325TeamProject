import java.util.*;

/**
 * This class represents an instructor user.
 */
public class Instructor extends User {

    private String instructorCode;

    private ArrayList<CourseClass> classes;

    public Instructor() {

        this("", "", "", "", "", "");

    }

    public Instructor(String userId, String email, String password, String name, String role, String instructorCode) {

        super(userId, email, password, name, role);

        this.instructorCode = instructorCode;

        this.classes = new ArrayList<CourseClass>();

    }

    public Instructor(int userId, String email, String password, String name, String role, String instructorCode) {

        this(String.valueOf(userId), email, password, name, role, instructorCode);

    }

    public Instructor(String userId, String name, String email, String password) {

        this(userId, email, password, name, "instructor", "");

    }

    public String getInstructorCode() {
        return instructorCode;
    }

    public void setInstructorCode(String instructorCode) {
        this.instructorCode = instructorCode;
    }

    public ArrayList<CourseClass> getClasses() {
        return classes;
    }

    public void setClasses(ArrayList<CourseClass> classes) {
        this.classes = classes;
    }

}
