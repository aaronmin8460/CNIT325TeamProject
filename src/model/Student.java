package model;

import java.util.ArrayList;

/**
 * This class represents a student user.
 */
public class Student extends User {

    private String studentNumber;

    private String classCode;

    private ArrayList<Attempt> attempts;

    public Student() {

        this(0, "", "", "", "", "", "");

    }

    public Student(int userId, String email, String password, String name, String role, String studentNumber, String classCode) {

        super(userId, email, password, name, role);

        this.studentNumber = studentNumber;

        this.classCode = classCode;

        this.attempts = new ArrayList<Attempt>();

    }

    public String getStudentNumber() { return studentNumber; }

    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getClassCode() { return classCode; }

    public void setClassCode(String classCode) { this.classCode = classCode; }

    public ArrayList<Attempt> getAttempts() { return attempts; }

    public void setAttempts(ArrayList<Attempt> attempts) { this.attempts = attempts; }

}
