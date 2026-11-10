package storage;

import model.Appointment;
import model.AppointmentStatus;
import model.ServiceType;
import util.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class AppointmentFileManager extends FileManager<Appointment> {

    public AppointmentFileManager() {
        super("data/appointments.txt");
    }

    // appointmentId|customerId|counterStaffId|technicianId|serviceType|startTime|status|price|taskDescription
    @Override
    protected Appointment parseLine(String line) {
        String[] parts = split(line);
        String taskDescription = "";
        if (parts.length > 8) {   // older lines may not have a task description
            taskDescription = parts[8];
        }
        return new Appointment(parts[0], parts[1], parts[2], parts[3], ServiceType.valueOf(parts[4]),
                DateUtil.parse(parts[5]), AppointmentStatus.valueOf(parts[6]), Double.parseDouble(parts[7]),
                taskDescription);
    }

    // Returns the appointments of one customer
    public List<Appointment> findByCustomer(String customerId) {
        List<Appointment> list = new ArrayList<>();
        for (Appointment appointment : readAll()) {
            if (appointment.getCustomerId().equals(customerId)) {
                list.add(appointment);
            }
        }
        return list;
    }

    // Returns the appointments of one technician
    public List<Appointment> findByTechnician(String technicianId) {
        List<Appointment> list = new ArrayList<>();
        for (Appointment appointment : readAll()) {
            if (appointment.getTechnicianId().equals(technicianId)) {
                list.add(appointment);
            }
        }
        return list;
    }

    // Returns the appointments with the given status
    public List<Appointment> findByStatus(AppointmentStatus status) {
        List<Appointment> list = new ArrayList<>();
        for (Appointment appointment : readAll()) {
            if (appointment.getStatus() == status) {
                list.add(appointment);
            }
        }
        return list;
    }
}
