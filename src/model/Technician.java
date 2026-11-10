package model;

import exception.ValidationException;
import storage.DataStore;
import ui.TechnicianDashboard;
import util.ActivityLog;
import util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.List;

public class Technician extends User {

    public Technician(String userId, String username, String password,
                      String name, String email, String phone) {
        super(userId, username, password, name, email, phone, Role.TECHNICIAN);
    }

    // Returns the appointments assigned to this technician
    public List<Appointment> viewAssignedAppointments() {
        return DataStore.appointments().findByTechnician(getUserId());
    }

    // Returns one appointment, if it is assigned to this technician
    public Appointment viewAppointmentDetails(String aptId) throws ValidationException {
        Appointment appointment = DataStore.appointments().findById(aptId);
        if (appointment == null || !getUserId().equals(appointment.getTechnicianId())) {
            throw new ValidationException("This appointment is not assigned to you.");
        }
        return appointment;
    }

    // Returns the customer comments of one appointment
    public List<Comment> viewComments(String aptId) throws ValidationException {
        viewAppointmentDetails(aptId);
        return DataStore.comments().findByAppointment(aptId);
    }

    // Marks an appointment as Completed
    public void markCompleted(String aptId) throws ValidationException {
        Appointment appointment = viewAppointmentDetails(aptId);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ValidationException("This appointment is already completed.");
        }
        if (appointment.getStartTime().isAfter(LocalDateTime.now())) {
            throw new ValidationException("You cannot complete an appointment that has not started yet.");
        }
        appointment.markCompleted();
        DataStore.appointments().update(appointment);
        ActivityLog.write(getUserId() + " completed appointment " + aptId);
    }

    // Saves (or updates) the feedback for a completed appointment
    public Feedback provideFeedback(String aptId, String text) throws ValidationException {
        Appointment appointment = viewAppointmentDetails(aptId);
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new ValidationException("Please mark the appointment as Completed before giving feedback.");
        }
        ValidationUtil.validateText("Feedback", text, 5, 300);

        // An appointment has 0..1 feedback, so an existing one is updated
        Feedback existing = DataStore.feedback().findByAppointment(aptId);
        if (existing != null) {
            existing.setText(ValidationUtil.clean(text));
            existing.setCreatedAt(LocalDateTime.now());
            DataStore.feedback().update(existing);
            ActivityLog.write(getUserId() + " updated feedback for " + aptId);
            return existing;
        }
        Feedback newFeedback = new Feedback(DataStore.feedback().nextId("F"), aptId, getUserId(),
                ValidationUtil.clean(text), LocalDateTime.now());
        DataStore.feedback().add(newFeedback);
        ActivityLog.write(getUserId() + " gave feedback for " + aptId);
        return newFeedback;
    }

    // Returns true if the technician has no appointment between start and end
    public boolean isAvailable(LocalDateTime start, LocalDateTime end) {
        return isAvailable(start, end, null);
    }

    // Overloaded version used when reassigning (ignores the appointment being moved)
    public boolean isAvailable(LocalDateTime start, LocalDateTime end, String ignoreAppointmentId) {
        for (Appointment appointment : viewAssignedAppointments()) {
            if (appointment.getAppointmentId().equals(ignoreAppointmentId)) {
                continue;
            }
            if (appointment.overlaps(start, end)) {
                return false;
            }
        }
        return true;
    }

    // Opens the technician window
    @Override
    public void openDashboard() {
        new TechnicianDashboard(this);
    }
}
