package service;

import java.util.ArrayList;

import model.Attempt;
import model.CourseClass;
import model.Instructor;
import model.Question;
import model.Student;
import model.User;

/**
 * This class is a placeholder for future Supabase database work.
 */
public class SupabaseService implements DataService {

    private String projectUrl;

    private String apiKey;

    public SupabaseService() {

        this("", "");

    }

    public SupabaseService(String projectUrl, String apiKey) {

        this.projectUrl = projectUrl;

        this.apiKey = apiKey;

    }

    @Override
    public User login(String email, String password) {

        // TODO: Check email and password with Supabase.
        return null;

    }

    @Override
    public CourseClass createClass(String className, Instructor instructor) {

        // TODO: Create a class in Supabase.
        return null;

    }

    @Override
    public boolean joinClass(String classCode, Student student) {

        // TODO: Add a student to a class in Supabase.
        return false;

    }

    @Override
    public CourseClass findClassByCode(String classCode) {

        // TODO: Find a class by code in Supabase.
        return null;

    }

    @Override
    public Question saveQuestion(String classCode, Question question) {

        // TODO: Save a question to Supabase.
        return null;

    }

    @Override
    public ArrayList<Question> getQuestionsForClass(String classCode) {

        // TODO: Load class questions from Supabase.
        return new ArrayList<Question>();

    }

    @Override
    public Attempt saveAttempt(Attempt attempt) {

        // TODO: Save the attempt to Supabase.
        return null;

    }

    @Override
    public ArrayList<Attempt> getAttemptsForClass(String classCode) {

        // TODO: Load class attempts from Supabase.
        return new ArrayList<Attempt>();

    }

    public String getProjectUrl() { return projectUrl; }

    public void setProjectUrl(String projectUrl) { this.projectUrl = projectUrl; }

    public String getApiKey() { return apiKey; }

    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

}
