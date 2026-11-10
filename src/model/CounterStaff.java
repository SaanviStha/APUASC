package model;

import exception.ValidationException;
import storage.DataStore;
import ui.CounterStaffDashboard;
import util.ActivityLog;
import util.DateUtil;
import util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CounterStaff extends User {

    public CounterStaff(String userId, String username, String password,
                        String name, String email, String phone) {
        super(userId, username, password, name, email, phone, Role.COUNTER_STAFF);
    }

    // =========================
    // CUSTOMERS (CRUD)
    // =========================

    // Registers a new customer after checking the details
    public void addCustomer(Customer c) throws ValidationException {
        ValidationUtil.validateCar(c.getCarModel(), c.getCarPlate());
        ValidationUtil.validateNewUser(c);
        c.setCarPlate(c.getCarPlate().trim().toUpperCase());
        c.setUserId(DataStore.users().nextId(Role.CUSTOMER.getIdPrefix()));
        DataStore.users().add(c);
        ActivityLog.write(getUserId() + " registered customer " + c.getUserId());
    }

    // Returns all customers
    public List<Customer> viewCustomers() {
        List<Customer> customers = new ArrayList<>();
        for (User user : DataStore.users().findByRole(Role.CUSTOMER)) {
            customers.add((Customer) user);
        }
        return customers;
    }

    // Saves changes to an existing customer
    public void updateCustomer(Customer c) throws ValidationException {
        if (!(DataStore.users().findById(c.getUserId()) instanceof Customer)) {
            throw new ValidationException("Please select a customer to update.");
        }
        ValidationUtil.validateCar(c.getCarModel(), c.getCarPlate());
        ValidationUtil.validateExistingUser(c);
        c.setCarPlate(c.getCarPlate().trim().toUpperCase());
        DataStore.users().update(c);
        ActivityLog.write(getUserId() + " updated customer " + c.getUserId());
    }

    // Deletes a customer who has no service history
    public void deleteCustomer(String customerId) throws ValidationException {
        User target = DataStore.users().findById(customerId);
        if (!(target instanceof Customer)) {
            throw new ValidationException("Please select a customer to delete.");
        }
        if (!DataStore.appointments().findByCustomer(customerId).isEmpty()) {
            throw new ValidationException(
                    "This customer has service history and cannot be deleted (records must be kept).");
        }
        DataStore.users().delete(customerId);
        ActivityLog.write(getUserId() + " deleted customer " + customerId);
    }

    // =========================
    // APPOINTMENTS
    // =========================

    // Returns the technicians who are free for the whole service
    public List<Technician> findAvailableTechs(ServiceType type, LocalDateTime start) {
        return findAvailableTechs(type, start, null);
    }

    // Same as above, but ignores one appointment (used when reassigning it)
    public List<Technician> findAvailableTechs(ServiceType type, LocalDateTime start,
                                               String ignoreAppointmentId) {
        LocalDateTime end = start.plusHours(type.getHours());
        List<Technician> available = new ArrayList<>();
        for (User user : DataStore.users().findByRole(Role.TECHNICIAN)) {
            Technician technician = (Technician) user;
            if (technician.isAvailable(start, end, ignoreAppointmentId)) {
                available.add(technician);
            }
        }
        return available;
    }

    // Creates a new appointment and assigns it to a free technician
    public Appointment createAppointment(String customerId, ServiceType type,
                                         LocalDateTime start, String technicianId,
                                         String taskDescription)
            throws ValidationException {
        if (!(DataStore.users().findById(customerId) instanceof Customer)) {
            throw new ValidationException("Please select a valid customer.");
        }
        DateUtil.validateBookingTime(start, type);
        Technician tech = getTechnician(technicianId);
        ValidationUtil.validateText("Task description", taskDescription, 5, 200);
        if (!tech.isAvailable(start, start.plusHours(type.getHours()))) {
            throw new ValidationException(tech.getName() + " is not available at that time.");
        }
        double price = DataStore.prices().getPrice(type).getAmount();
        Appointment appointment = new Appointment(DataStore.appointments().nextId("A"), customerId, getUserId(),
                technicianId, type, start, AppointmentStatus.ASSIGNED, price,
                ValidationUtil.clean(taskDescription));
        DataStore.appointments().add(appointment);
        ActivityLog.write(getUserId() + " created appointment " + appointment.getAppointmentId()
                + " and assigned the task to " + technicianId);
        return appointment;
    }

    // Moves an unfinished appointment to another free technician
    public void assignTechnician(String aptId, String techId) throws ValidationException {
        Appointment appointment = DataStore.appointments().findById(aptId);
        if (appointment == null) {
            throw new ValidationException("Appointment not found.");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ValidationException("A completed appointment cannot be reassigned.");
        }
        Technician tech = getTechnician(techId);
        if (!tech.isAvailable(appointment.getStartTime(), appointment.getEndTime(), aptId)) {
            throw new ValidationException(tech.getName() + " is not available at that time.");
        }
        appointment.setTechnicianId(techId);
        DataStore.appointments().update(appointment);
        ActivityLog.write(getUserId() + " reassigned appointment " + aptId + " to " + techId);
    }

    private Technician getTechnician(String techId) throws ValidationException {
        User user = null;
        if (techId != null) {
            user = DataStore.users().findById(techId);
        }
        if (!(user instanceof Technician)) {
            throw new ValidationException("Please select a technician.");
        }
        return (Technician) user;
    }

    // =========================
    // PAYMENTS
    // =========================

    // Records the payment of a completed appointment
    public Payment collectPayment(String aptId, String method) throws ValidationException {
        Appointment appointment = DataStore.appointments().findById(aptId);
        if (appointment == null) {
            throw new ValidationException("Please select an appointment.");
        }
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new ValidationException("Payment can only be collected after the service is completed.");
        }
        if (DataStore.payments().findByAppointment(aptId) != null) {
            throw new ValidationException("This appointment has already been paid.");
        }
        if (ValidationUtil.isBlank(method)) {
            throw new ValidationException("Please choose a payment method.");
        }
        Payment payment = new Payment(DataStore.payments().nextId("P"), aptId, getUserId(),
                appointment.getPrice(), method, LocalDateTime.now());
        DataStore.payments().add(payment);
        ActivityLog.write(getUserId() + " collected payment " + payment.getPaymentId() + " for " + aptId);
        return payment;
    }

    // Creates the receipt of a payment
    public Receipt generateReceipt(String paymentId) throws ValidationException {
        Payment payment = DataStore.payments().findById(paymentId);
        if (payment == null) {
            throw new ValidationException("Payment not found.");
        }
        return payment.generateReceipt();
    }

    // Opens the counter staff window
    @Override
    public void openDashboard() {
        new CounterStaffDashboard(this);
    }
}
