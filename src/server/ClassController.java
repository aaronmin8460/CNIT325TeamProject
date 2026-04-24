package server;

import java.util.HashMap;

import model.CourseClass;
import model.Instructor;
import model.Student;
import service.DataService;

/**
 * This class stores class list information for the server.
 */
public class ClassController {

    private DataService dataService;

    private HashMap<String, CourseClass> classesByCode;

    public ClassController() {

        this(null);

    }

    public ClassController(DataService dataService) {

        this.dataService = dataService;

        this.classesByCode = new HashMap<String, CourseClass>();

    }

    public CourseClass createClass(String className, Instructor instructor) {

        CourseClass courseClass;

        if (dataService == null) {
            return null;
        }

        courseClass = dataService.createClass(className, instructor);

        if (courseClass != null) {
            classesByCode.put(courseClass.getClassCode(), courseClass);
        }

        return courseClass;

    }

    public boolean joinClass(String classCode, Student student) {

        if (dataService == null) {
            return false;
        }

        return dataService.joinClass(classCode, student);

    }

    public CourseClass findClassByCode(String classCode) {

        CourseClass courseClass;

        if (classCode == null) {
            return null;
        }

        courseClass = classesByCode.get(classCode);

        if (courseClass != null) {
            return courseClass;
        }

        if (dataService == null) {
            return null;
        }

        courseClass = dataService.findClassByCode(classCode);

        if (courseClass != null) {
            classesByCode.put(classCode, courseClass);
        }

        return courseClass;

    }

    public DataService getDataService() { return dataService; }

    public void setDataService(DataService dataService) { this.dataService = dataService; }

    public HashMap<String, CourseClass> getClassesByCode() { return classesByCode; }

    public void setClassesByCode(HashMap<String, CourseClass> classesByCode) { this.classesByCode = classesByCode; }

}
