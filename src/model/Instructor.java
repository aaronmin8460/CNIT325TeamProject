package model;

import java.util.ArrayList;

/**
 * This class represents an instructor user.
 */
public class Instructor extends User {

    private String instructorCode;

    private ArrayList<CourseClass> classes;

    public Instructor() {

        this(0, "", "", "", "", "");

    }

    public Instructor(int userId, String email, String password, String name, String role, String instructorCode) {

        super(userId, email, password, name, role);

        this.instructorCode = instructorCode;

        this.classes = new ArrayList<CourseClass>();

    }

    public String getInstructorCode() { return instructorCode; }

    public void setInstructorCode(String instructorCode) { this.instructorCode = instructorCode; }

    public ArrayList<CourseClass> getClasses() { return classes; }

    public void setClasses(ArrayList<CourseClass> classes) { this.classes = classes; }

}
