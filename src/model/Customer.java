package model;

import exception.ValidationException;
import storage.DataStore;
import ui.CustomerDashboard;
import util.ActivityLog;
import util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Customer extends User {

    private String carModel;
    private String carPlate;

    public Customer(String userId, String username, String password, String name,
                    String email, String phone, String carModel, String carPlate) {
        super(userId, username, password, name, email, phone, Role.CUSTOMER);
        this.carModel = carModel;
        this.carPlate = carPlate;
    }

    // Returns all appointments of this customer
    public List<Appointment> viewServiceHistory() {
        return DataStore.appointments().findByCustomer(getUserId());
    }

    // Returns all payments made by this customer
    public List<Payment> viewPaymentHistory() {
        List<Payment> list = new ArrayList<>();
        for (Appointment appointment : viewServiceHistory()) {
            Payment payment = DataStore.payments().findByAppointment(appointment.getAppointmentId());
            if (payment != null) {
                list.add(payment);
            }
        }
        return list;
    }

    // Returns the technician's feedback for one of this customer's appointments
    public Feedback viewFeedback(String aptId) throws ValidationException {
        checkOwnAppointment(aptId);
        return DataStore.feedback().findByAppointment(aptId);
    }

    // Saves a comment about a staff member involved in the appointment
    public Comment addComment(String aptId, String staffId, String text) throws ValidationException {
        Appointment appointment = checkOwnAppointment(aptId);
        if (staffId == null
                || (!staffId.equals(appointment.getCounterStaffId()) && !staffId.equals(appointment.getTechnicianId()))) {
            throw new ValidationException("You can only comment on staff involved in this appointment.");
        }
        ValidationUtil.validateText("Comment", text, 3, 300);
        Comment comment = new Comment(DataStore.comments().nextId("CM"), aptId, getUserId(), staffId,
                ValidationUtil.clean(text), LocalDateTime.now());
        DataStore.comments().add(comment);
        ActivityLog.write(getUserId() + " commented on " + staffId + " for " + aptId);
        return comment;
    }

    // Customers also update their car details from the profile page (overloading)
    public void editProfile(String name, String email, String phone, String password,
                            String carModel, String carPlate) throws ValidationException {
        ValidationUtil.validateCar(carModel, carPlate);
        ValidationUtil.validatePerson(name, email, phone, password);
        this.carModel = carModel.trim();
        this.carPlate = carPlate.trim().toUpperCase();
        super.editProfile(name, email, phone, password);
    }

    // Makes sure the appointment belongs to this customer
    private Appointment checkOwnAppointment(String aptId) throws ValidationException {
        Appointment appointment = null;
        if (aptId != null) {
            appointment = DataStore.appointments().findById(aptId);
        }
        if (appointment == null || !getUserId().equals(appointment.getCustomerId())) {
            throw new ValidationException("Please select one of your appointments.");
        }
        return appointment;
    }

    // Adds the car details to the line saved in users.txt (overrides User)
    @Override
    public String toFileLine() {
        return super.toFileLine() + DELIMITER + carModel + DELIMITER + carPlate;
    }

    // Opens the customer window
    @Override
    public void openDashboard() {
        new CustomerDashboard(this);
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public String getCarPlate() {
        return carPlate;
    }

    public void setCarPlate(String carPlate) {
        this.carPlate = carPlate;
    }
}
