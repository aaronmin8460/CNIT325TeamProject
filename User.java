/**
 * This class is the parent class for all users.
 */
public class User {

    private String userId;

    private String email;

    private String password;

    private String name;

    private String role;

    public User() {

        this("", "", "", "", "");

    }

    public User(String userId, String email, String password, String name, String role) {

        this.userId = userId;

        this.email = email;

        this.password = password;

        this.name = name;

        this.role = role;

    }

    public User(int userId, String email, String password, String name, String role) {

        this(String.valueOf(userId), email, password, name, role);

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserId(int userId) {
        this.userId = String.valueOf(userId);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
