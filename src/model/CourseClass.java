package model;

import java.util.ArrayList;

/**
 * This class stores one course or class section.
 */
public class CourseClass {

    private String classId;

    private String classCode;

    private String className;

    private Instructor instructor;

    private ArrayList<Student> students;

    private ArrayList<Question> questions;

    public CourseClass() {

        this("", "");

    }

    public CourseClass(String classCode, String className) {

        this.classId = "";

        this.classCode = classCode;

        this.className = className;

        this.students = new ArrayList<Student>();

        this.questions = new ArrayList<Question>();

    }

    public String getClassId() { return classId; }

    public void setClassId(String classId) { this.classId = classId; }

    public String getClassCode() { return classCode; }

    public void setClassCode(String classCode) { this.classCode = classCode; }

    public String getClassName() { return className; }

    public void setClassName(String className) { this.className = className; }

    public Instructor getInstructor() { return instructor; }

    public void setInstructor(Instructor instructor) { this.instructor = instructor; }

    public ArrayList<Student> getStudents() { return students; }

    public void setStudents(ArrayList<Student> students) { this.students = students; }

    public ArrayList<Question> getQuestions() { return questions; }

    public void setQuestions(ArrayList<Question> questions) { this.questions = questions; }

}
