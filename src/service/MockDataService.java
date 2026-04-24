package service;

import java.util.ArrayList;
import java.util.HashMap;

import model.Attempt;
import model.CourseClass;
import model.Instructor;
import model.Question;
import model.Student;
import model.User;

/**
 * This class stores simple in-memory data for testing.
 */
public class MockDataService implements DataService {

    private ArrayList<User> users;

    private ArrayList<CourseClass> classes;

    private HashMap<String, User> usersByEmail;

    private HashMap<String, CourseClass> classesByCode;

    private HashMap<String, ArrayList<Student>> classMemberships;

    private ArrayList<Question> questions;

    private HashMap<String, ArrayList<Question>> questionsByClassCode;

    private ArrayList<Attempt> attempts;

    private HashMap<String, ArrayList<Attempt>> attemptsByClassCode;

    private int nextClassNumber;

    private int nextQuestionId;

    private int nextAttemptId;

    public MockDataService() {

        this.users = new ArrayList<User>();

        this.classes = new ArrayList<CourseClass>();

        this.usersByEmail = new HashMap<String, User>();

        this.classesByCode = new HashMap<String, CourseClass>();

        this.classMemberships = new HashMap<String, ArrayList<Student>>();

        this.questions = new ArrayList<Question>();

        this.questionsByClassCode = new HashMap<String, ArrayList<Question>>();

        this.attempts = new ArrayList<Attempt>();

        this.attemptsByClassCode = new HashMap<String, ArrayList<Attempt>>();

        this.nextClassNumber = 1000;

        this.nextQuestionId = 1;

        this.nextAttemptId = 1;

        loadTestUsers();

    }

    private void loadTestUsers() {

        Instructor instructor;
        Student student;

        instructor = new Instructor(
            1,
            "instructor@test.com",
            "pass123",
            "Test Instructor",
            "instructor",
            "INS1001"
        );

        student = new Student(
            2,
            "student@test.com",
            "pass123",
            "Test Student",
            "student",
            "STU1001",
            ""
        );

        users.add(instructor);
        users.add(student);

        usersByEmail.put(instructor.getEmail(), instructor);
        usersByEmail.put(student.getEmail(), student);

    }

    @Override
    public User login(String email, String password) {

        int i;
        User user;

        if (email == null || password == null) {
            return null;
        }

        for (i = 0; i < users.size(); i++) {

            user = users.get(i);

            if (user != null) {
                if (email.equalsIgnoreCase(user.getEmail())) {
                    if (password.equals(user.getPassword())) {
                        return user;
                    }
                }
            }

        }

        return null;

    }

    @Override
    public CourseClass createClass(String className, Instructor instructor) {

        CourseClass courseClass;
        String classCode;
        ArrayList<CourseClass> instructorClasses;

        if (className == null || instructor == null) {
            return null;
        }

        classCode = "CLS" + nextClassNumber;
        nextClassNumber++;

        courseClass = new CourseClass(classCode, className);
        courseClass.setInstructor(instructor);

        classes.add(courseClass);
        classesByCode.put(classCode, courseClass);
        classMemberships.put(classCode, courseClass.getStudents());
        questionsByClassCode.put(classCode, courseClass.getQuestions());
        attemptsByClassCode.put(classCode, new ArrayList<Attempt>());

        instructorClasses = instructor.getClasses();

        if (instructorClasses == null) {
            instructorClasses = new ArrayList<CourseClass>();
            instructor.setClasses(instructorClasses);
        }

        instructorClasses.add(courseClass);

        addUserIfNeeded(instructor);

        return courseClass;

    }

    @Override
    public boolean joinClass(String classCode, Student student) {

        CourseClass courseClass;
        ArrayList<Student> studentsInClass;
        int i;
        Student currentStudent;

        if (classCode == null || student == null) {
            return false;
        }

        courseClass = findClassByCode(classCode);

        if (courseClass == null) {
            return false;
        }

        studentsInClass = classMemberships.get(classCode);

        if (studentsInClass == null) {
            studentsInClass = courseClass.getStudents();

            if (studentsInClass == null) {
                studentsInClass = new ArrayList<Student>();
                courseClass.setStudents(studentsInClass);
            }

            classMemberships.put(classCode, studentsInClass);
        }

        for (i = 0; i < studentsInClass.size(); i++) {

            currentStudent = studentsInClass.get(i);

            if (currentStudent != null) {
                if (student.getEmail().equalsIgnoreCase(currentStudent.getEmail())) {
                    return false;
                }
            }

        }

        studentsInClass.add(student);
        student.setClassCode(classCode);

        addUserIfNeeded(student);

        return true;

    }

    @Override
    public CourseClass findClassByCode(String classCode) {

        if (classCode == null) {
            return null;
        }

        return classesByCode.get(classCode);

    }

    @Override
    public Question saveQuestion(String classCode, Question question) {

        CourseClass courseClass;
        ArrayList<Question> classQuestions;

        if (classCode == null || question == null) {
            return null;
        }

        courseClass = findClassByCode(classCode);

        if (courseClass == null) {
            return null;
        }

        if (question.getQuestionId() == null || question.getQuestionId().length() == 0) {
            question.setQuestionId(nextQuestionId);
            nextQuestionId++;
        }

        question.setClassCode(classCode);

        questions.add(question);

        classQuestions = questionsByClassCode.get(classCode);

        if (classQuestions == null) {
            classQuestions = courseClass.getQuestions();

            if (classQuestions == null) {
                classQuestions = new ArrayList<Question>();
                courseClass.setQuestions(classQuestions);
            }

            questionsByClassCode.put(classCode, classQuestions);
        }

        classQuestions.add(question);

        return question;

    }

    @Override
    public ArrayList<Question> getQuestionsForClass(String classCode) {

        ArrayList<Question> storedQuestions;
        ArrayList<Question> result;
        int i;

        result = new ArrayList<Question>();

        if (classCode == null) {
            return result;
        }

        storedQuestions = questionsByClassCode.get(classCode);

        if (storedQuestions == null) {
            return result;
        }

        for (i = 0; i < storedQuestions.size(); i++) {
            result.add(storedQuestions.get(i));
        }

        return result;

    }

    @Override
    public Attempt saveAttempt(Attempt attempt) {

        Question question;
        String classCode;
        CourseClass courseClass;
        ArrayList<Attempt> classAttempts;
        Student student;
        ArrayList<Attempt> studentAttempts;

        if (attempt == null) {
            return null;
        }

        question = attempt.getQuestion();

        if (question == null) {
            return null;
        }

        classCode = question.getClassCode();

        if (classCode == null || classCode.length() == 0) {
            return null;
        }

        courseClass = findClassByCode(classCode);

        if (courseClass == null) {
            return null;
        }

        if (attempt.getAttemptId() <= 0) {
            attempt.setAttemptId(nextAttemptId);
            nextAttemptId++;
        }

        attempts.add(attempt);

        classAttempts = attemptsByClassCode.get(classCode);

        if (classAttempts == null) {
            classAttempts = new ArrayList<Attempt>();
            attemptsByClassCode.put(classCode, classAttempts);
        }

        classAttempts.add(attempt);

        student = attempt.getStudent();

        if (student != null) {
            studentAttempts = student.getAttempts();

            if (studentAttempts == null) {
                studentAttempts = new ArrayList<Attempt>();
                student.setAttempts(studentAttempts);
            }

            studentAttempts.add(attempt);
            addUserIfNeeded(student);
        }

        return attempt;

    }

    @Override
    public ArrayList<Attempt> getAttemptsForClass(String classCode) {

        ArrayList<Attempt> storedAttempts;
        ArrayList<Attempt> result;
        int i;

        result = new ArrayList<Attempt>();

        if (classCode == null) {
            return result;
        }

        storedAttempts = attemptsByClassCode.get(classCode);

        if (storedAttempts == null) {
            return result;
        }

        for (i = 0; i < storedAttempts.size(); i++) {
            result.add(storedAttempts.get(i));
        }

        return result;

    }

    private void addUserIfNeeded(User user) {

        int i;
        User currentUser;

        if (user == null) {
            return;
        }

        for (i = 0; i < users.size(); i++) {

            currentUser = users.get(i);

            if (currentUser != null) {
                if (user.getEmail().equalsIgnoreCase(currentUser.getEmail())) {
                    usersByEmail.put(currentUser.getEmail(), currentUser);
                    return;
                }
            }

        }

        users.add(user);
        usersByEmail.put(user.getEmail(), user);

    }

    public void setUsers(ArrayList<User> users) { this.users = users; }

    public void setClasses(ArrayList<CourseClass> classes) { this.classes = classes; }

    public HashMap<String, User> getUsersByEmail() { return usersByEmail; }

    public void setUsersByEmail(HashMap<String, User> usersByEmail) { this.usersByEmail = usersByEmail; }

    public HashMap<String, CourseClass> getClassesByCode() { return classesByCode; }

    public void setClassesByCode(HashMap<String, CourseClass> classesByCode) { this.classesByCode = classesByCode; }

    public HashMap<String, ArrayList<Student>> getClassMemberships() { return classMemberships; }

    public void setClassMemberships(HashMap<String, ArrayList<Student>> classMemberships) { this.classMemberships = classMemberships; }

    public ArrayList<User> getUsers() { return users; }

    public ArrayList<CourseClass> getClasses() { return classes; }

    public ArrayList<Question> getQuestions() { return questions; }

    public void setQuestions(ArrayList<Question> questions) { this.questions = questions; }

    public HashMap<String, ArrayList<Question>> getQuestionsByClassCode() { return questionsByClassCode; }

    public void setQuestionsByClassCode(HashMap<String, ArrayList<Question>> questionsByClassCode) { this.questionsByClassCode = questionsByClassCode; }

    public ArrayList<Attempt> getAttempts() { return attempts; }

    public void setAttempts(ArrayList<Attempt> attempts) { this.attempts = attempts; }

    public HashMap<String, ArrayList<Attempt>> getAttemptsByClassCode() { return attemptsByClassCode; }

    public void setAttemptsByClassCode(HashMap<String, ArrayList<Attempt>> attemptsByClassCode) { this.attemptsByClassCode = attemptsByClassCode; }

}
