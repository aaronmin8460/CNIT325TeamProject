package server;

import java.util.HashMap;

import model.User;
import service.DataService;

/**
 * This class stores login and user lookup information for the server.
 */
public class AuthController {

    private DataService dataService;

    private HashMap<String, User> usersByEmail;

    public AuthController() {

        this(null);

    }

    public AuthController(DataService dataService) {

        this.dataService = dataService;

        this.usersByEmail = new HashMap<String, User>();

    }

    public User login(String email, String password) {

        User user;

        if (dataService == null) {
            return null;
        }

        user = dataService.login(email, password);

        if (user != null) {
            usersByEmail.put(user.getEmail(), user);
        }

        return user;

    }

    public DataService getDataService() { return dataService; }

    public void setDataService(DataService dataService) { this.dataService = dataService; }

    public HashMap<String, User> getUsersByEmail() { return usersByEmail; }

    public void setUsersByEmail(HashMap<String, User> usersByEmail) { this.usersByEmail = usersByEmail; }

}
