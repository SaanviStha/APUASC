package model;

import exception.InvalidLoginException;
import exception.ValidationException;
import storage.DataStore;
import util.ActivityLog;
import util.ValidationUtil;

/**
 * Base class of every person who can log in to APU-ASC.
 * Shows abstraction (openDashboard), encapsulation (private fields)
 * and is the parent for inheritance.
 */
public abstract class User implements Storable {

    private String userId;
    private String username;
    private String password;
    private String name;
    private String email;
    private String phone;
    private final Role role;

    protected User(String userId, String username, String password,
                   String name, String email, String phone, Role role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    // =========================
    // LOGIN / LOGOUT
    // =========================

    // Checks the username and password and returns the matching user
    public static User login(String username, String password) throws InvalidLoginException {
        if (ValidationUtil.isBlank(username)) {
            throw new InvalidLoginException("Username is required.");
        }
        if (ValidationUtil.isBlank(password)) {
            throw new InvalidLoginException("Password is required.");
        }
        User user = DataStore.users().findByUsername(username.trim());
        if (user == null || !user.checkPassword(password)) {
            ActivityLog.write("FAILED LOGIN for username '" + username.trim() + "'");
            throw new InvalidLoginException("Invalid username or password.");
        }
        ActivityLog.write(user.getUserId() + " (" + user.getRole() + ") logged in");
        return user;
    }

    // Returns true if the password is correct
    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    // Records the logout in the activity log
    public void logout() {
        ActivityLog.write(userId + " (" + role + ") logged out");
    }

    // =========================
    // PROFILE
    // =========================

    // Checks and saves new personal details
    public void editProfile(String name, String email, String phone, String password)
            throws ValidationException {
        ValidationUtil.validatePerson(name, email, phone, password);
        this.name = name.trim();
        this.email = email.trim();
        this.phone = phone.trim();
        this.password = password;
        DataStore.users().update(this);
        ActivityLog.write(userId + " updated own profile");
    }

    // Each role opens its own dashboard (polymorphism)
    public abstract void openDashboard();

    // =========================
    // STORABLE
    // =========================

    @Override
    public String getId() {
        return userId;
    }

    // Turns the user into one line of text for users.txt
    @Override
    public String toFileLine() {
        return String.join(DELIMITER, userId, username, password, name, email, phone, role.name());
    }

    // =========================
    // GETTERS AND SETTERS
    // =========================

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    // Text shown when the user appears in a list, e.g. "Roshni Kumari (T002)"
    @Override
    public String toString() {
        return name + " (" + userId + ")";
    }
}
