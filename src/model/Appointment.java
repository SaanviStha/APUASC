package model;

import util.DateUtil;

import java.time.LocalDateTime;

public class Appointment implements Storable {

    private String appointmentId;
    private String customerId;
    private String counterStaffId;
    private String technicianId;
    private ServiceType serviceType;
    private LocalDateTime startTime;
    private AppointmentStatus status;
    private double price;
    private String taskDescription;

    public Appointment(String appointmentId, String customerId, String counterStaffId,
                       String technicianId, ServiceType serviceType, LocalDateTime startTime,
                       AppointmentStatus status, double price, String taskDescription) {
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.counterStaffId = counterStaffId;
        this.technicianId = technicianId;
        this.serviceType = serviceType;
        this.startTime = startTime;
        this.status = status;
        this.price = price;
        this.taskDescription = taskDescription;
    }

    // Normal service = 1 hour, major service = 3 hours
    public LocalDateTime getEndTime() {
        return startTime.plusHours(serviceType.getHours());
    }

    // Two time slots overlap when one starts before the other ends
    public boolean overlaps(LocalDateTime start, LocalDateTime end) {
        return start.isBefore(getEndTime()) && end.isAfter(startTime);
    }

    // Changes the status of the appointment to Completed
    public void markCompleted() {
        status = AppointmentStatus.COMPLETED;
    }

    @Override
    public String getId() {
        return appointmentId;
    }

    // Turns the appointment into one line of text for appointments.txt
    @Override
    public String toFileLine() {
        return String.join(DELIMITER, appointmentId, customerId, counterStaffId, technicianId,
                serviceType.name(), DateUtil.format(startTime), status.name(),
                String.format("%.2f", price), taskDescription);
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCounterStaffId() {
        return counterStaffId;
    }

    public String getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(String technicianId) {
        this.technicianId = technicianId;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    // Returns the price that was charged when the appointment was booked
    public double getPrice() {
        return price;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    // Text shown when the appointment appears in a list
    @Override
    public String toString() {
        return appointmentId + " - " + serviceType.getDisplayName() + " on " + DateUtil.format(startTime);
    }
}
