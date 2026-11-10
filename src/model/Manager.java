package model;

import exception.ValidationException;
import storage.DataStore;
import ui.ManagerDashboard;
import util.ActivityLog;
import util.ValidationUtil;

import java.util.ArrayList;
import java.util.List;

public class Manager extends User {

    public Manager(String userId, String username, String password,
                   String name, String email, String phone) {
        super(userId, username, password, name, email, phone, Role.MANAGER);
    }

    // =========================
    // STAFF ACCOUNTS (CRUD)
    // =========================

    // Creates a new manager, counter staff or technician account
    public void addStaff(User user) throws ValidationException {
        checkStaffRole(user);
        ValidationUtil.validateNewUser(user);
        user.setUserId(DataStore.users().nextId(user.getRole().getIdPrefix()));
        DataStore.users().add(user);
        ActivityLog.write(getUserId() + " added staff " + user.getUserId());
    }

    // Returns every account that is not a customer
    public List<User> viewStaff() {
        List<User> staff = new ArrayList<>();
        for (User user : DataStore.users().readAll()) {
            if (user.getRole() != Role.CUSTOMER) {
                staff.add(user);
            }
        }
        return staff;
    }

    // Saves changes to an existing staff account
    public void updateStaff(User user) throws ValidationException {
        checkStaffRole(user);
        User old = DataStore.users().findById(user.getUserId());
        if (old == null) {
            throw new ValidationException("Please select a staff account to update.");
        }
        if (old.getRole() != user.getRole()) {
            throw new ValidationException("The role of an existing account cannot be changed.");
        }
        ValidationUtil.validateExistingUser(user);
        DataStore.users().update(user);
        ActivityLog.write(getUserId() + " updated staff " + user.getUserId());
    }

    // Deletes a staff account if the business rules allow it
    public void deleteStaff(String userId) throws ValidationException {
        User target = DataStore.users().findById(userId);
        if (target == null || target.getRole() == Role.CUSTOMER) {
            throw new ValidationException("Please select a staff account to delete.");
        }
        if (target.getUserId().equals(getUserId())) {
            throw new ValidationException("You cannot delete your own account while logged in.");
        }
        if (target.getRole() == Role.MANAGER && DataStore.users().findByRole(Role.MANAGER).size() <= 1) {
            throw new ValidationException("The system must keep at least one manager.");
        }
        if (target.getRole() == Role.TECHNICIAN) {
            for (Appointment appointment : DataStore.appointments().findByTechnician(userId)) {
                if (appointment.getStatus() == AppointmentStatus.ASSIGNED) {
                    throw new ValidationException(
                            "This technician still has assigned appointments. Reassign them first.");
                }
            }
        }
        DataStore.users().delete(userId);
        ActivityLog.write(getUserId() + " deleted staff " + userId);
    }

    // Makes sure the account is not a customer
    private void checkStaffRole(User user) throws ValidationException {
        if (user.getRole() == Role.CUSTOMER) {
            throw new ValidationException(
                    "Managers can only manage manager, counter staff and technician accounts.");
        }
    }

    // =========================
    // PRICES, FEEDBACK, REPORTS
    // =========================

    public void setPrice(ServiceType type, double amount) throws ValidationException {
        if (amount <= 0 || amount > 100000) {
            throw new ValidationException("Price must be more than RM 0 and at most RM 100,000.");
        }
        ServicePrice price = DataStore.prices().getPrice(type);
        price.setAmount(amount);
        DataStore.prices().update(price);
        ActivityLog.write(getUserId() + " set " + type.name() + " price to RM "
                + String.format("%.2f", amount));
    }

    // Returns all feedback written by technicians
    public List<Feedback> viewAllFeedback() {
        return DataStore.feedback().readAll();
    }

    // Returns all comments written by customers
    public List<Comment> viewAllComments() {
        return DataStore.comments().readAll();
    }

    // Creates a report from all appointments and payments
    public Report generateReport() {
        return new Report(DataStore.appointments().readAll(), DataStore.payments().readAll());
    }

    // Opens the manager window
    @Override
    public void openDashboard() {
        new ManagerDashboard(this);
    }
}
