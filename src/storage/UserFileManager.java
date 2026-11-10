package storage;

import model.CounterStaff;
import model.Customer;
import model.Manager;
import model.Role;
import model.Technician;
import model.User;

import java.util.ArrayList;
import java.util.List;

public class UserFileManager extends FileManager<User> {

    public UserFileManager() {
        super("data/users.txt");
    }

    // userId|username|password|name|email|phone|role[|carModel|carPlate]
    @Override
    protected User parseLine(String line) {
        String[] parts = split(line);
        Role role = Role.valueOf(parts[6]);
        switch (role) {
            case MANAGER:
                return new Manager(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
            case COUNTER_STAFF:
                return new CounterStaff(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
            case TECHNICIAN:
                return new Technician(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
            case CUSTOMER:
                return new Customer(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[7], parts[8]);
            default:
                return null;
        }
    }

    // Returns the user with this username, or null
    public User findByUsername(String username) {
        for (User user : readAll()) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    // Returns all users with the given role
    public List<User> findByRole(Role role) {
        List<User> list = new ArrayList<>();
        for (User user : readAll()) {
            if (user.getRole() == role) {
                list.add(user);
            }
        }
        return list;
    }

    // e.g. "S001 - Sadiksha Timsina"
    public String idAndName(String userId) {
        User user = findById(userId);
        if (user == null) {
            return userId + " - (removed)";
        }
        return user.getUserId() + " - " + user.getName();
    }

    // Returns true if another user already has this username
    public boolean usernameTaken(String username, String exceptUserId) {
        User user = findByUsername(username);
        return user != null && !user.getUserId().equals(exceptUserId);
    }
}
