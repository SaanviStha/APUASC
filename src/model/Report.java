package model;

import storage.DataStore;
import util.DateUtil;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Report {

    private final LocalDate generatedOn;
    private final List<Appointment> appointments;
    private final List<Payment> payments;

    public Report(List<Appointment> appointments, List<Payment> payments) {
        this.generatedOn = LocalDate.now();
        this.appointments = appointments;
        this.payments = payments;
    }

    // Adds up all payments
    public double totalRevenue() {
        double total = 0;
        for (Payment payment : payments) {
            total += payment.getAmount();
        }
        return total;
    }

    // Adds up the prices of completed jobs that are not paid yet
    public double outstandingAmount() {
        double total = 0;
        for (Appointment appointment : appointments) {
            if (appointment.getStatus() == AppointmentStatus.COMPLETED
                    && DataStore.payments().findByAppointment(appointment.getAppointmentId()) == null) {
                total += appointment.getPrice();
            }
        }
        return total;
    }

    // Counts the appointments of each service type
    public Map<ServiceType, Integer> countByServiceType() {
        Map<ServiceType, Integer> map = new LinkedHashMap<>();
        for (ServiceType serviceType : ServiceType.values()) {
            map.put(serviceType, 0);
        }
        for (Appointment appointment : appointments) {
            map.put(appointment.getServiceType(), map.get(appointment.getServiceType()) + 1);
        }
        return map;
    }

    // Adds up the payments of each service type
    public Map<ServiceType, Double> revenueByServiceType() {
        Map<ServiceType, Double> map = new LinkedHashMap<>();
        for (ServiceType serviceType : ServiceType.values()) {
            map.put(serviceType, 0.0);
        }
        for (Payment payment : payments) {
            Appointment appointment = DataStore.appointments().findById(payment.getAppointmentId());
            if (appointment != null) {
                map.put(appointment.getServiceType(), map.get(appointment.getServiceType()) + payment.getAmount());
            }
        }
        return map;
    }

    // Counts the appointments of each status
    public Map<AppointmentStatus, Integer> countByStatus() {
        Map<AppointmentStatus, Integer> map = new LinkedHashMap<>();
        for (AppointmentStatus s : AppointmentStatus.values()) {
            map.put(s, 0);
        }
        for (Appointment appointment : appointments) {
            map.put(appointment.getStatus(), map.get(appointment.getStatus()) + 1);
        }
        return map;
    }

    // "T001 - Aashika Pokharel" -> number of jobs
    public Map<String, Integer> jobsPerTechnician() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (User user : DataStore.users().findByRole(Role.TECHNICIAN)) {
            map.put(DataStore.users().idAndName(user.getUserId()), 0);
        }
        for (Appointment appointment : appointments) {
            String key = DataStore.users().idAndName(appointment.getTechnicianId());
            if (map.containsKey(key)) {
                map.put(key, map.get(key) + 1);
            } else {
                map.put(key, 1);   // technician who was deleted later
            }
        }
        return map;
    }

    // "S001 - Sadiksha Timsina" -> total amount collected
    public Map<String, Double> paymentsPerCounterStaff() {
        Map<String, Double> map = new LinkedHashMap<>();
        for (User user : DataStore.users().findByRole(Role.COUNTER_STAFF)) {
            map.put(DataStore.users().idAndName(user.getUserId()), 0.0);
        }
        for (Payment payment : payments) {
            String key = DataStore.users().idAndName(payment.getCounterStaffId());
            if (map.containsKey(key)) {
                map.put(key, map.get(key) + payment.getAmount());
            } else {
                map.put(key, payment.getAmount());   // staff member who was deleted later
            }
        }
        return map;
    }

    // Builds the full report as text
    public String toText() {
        String line = "-".repeat(120) + "\n";
        StringBuilder text = new StringBuilder();
        text.append("APU AUTOMOTIVE SERVICE CENTRE - ANALYSED REPORT\n");
        text.append("Generated on: ").append(DateUtil.formatDate(generatedOn)).append("\n\n");
        text.append(String.format("Total appointments : %d%n", appointments.size()));
        text.append(String.format("Total revenue      : RM %.2f%n", totalRevenue()));
        text.append(String.format("Outstanding (unpaid completed jobs): RM %.2f%n%n", outstandingAmount()));

        text.append("Appointments by service type:\n");
        Map<ServiceType, Double> revenue = revenueByServiceType();
        for (Map.Entry<ServiceType, Integer> e : countByServiceType().entrySet()) {
            text.append(String.format("  %-16s %3d jobs   RM %.2f%n",
                    e.getKey().getDisplayName(), e.getValue(), revenue.get(e.getKey())));
        }
        text.append("\nAppointments by status:\n");
        for (Map.Entry<AppointmentStatus, Integer> e : countByStatus().entrySet()) {
            text.append(String.format("  %-16s %3d%n", e.getKey(), e.getValue()));
        }
        text.append("\nJobs per technician:\n");
        for (Map.Entry<String, Integer> e : jobsPerTechnician().entrySet()) {
            text.append(String.format("  %-30s %3d%n", e.getKey(), e.getValue()));
        }
        text.append("\nPayments collected per counter staff:\n");
        for (Map.Entry<String, Double> e : paymentsPerCounterStaff().entrySet()) {
            text.append(String.format("  %-30s RM %.2f%n", e.getKey(), e.getValue()));
        }

        text.append("\nAppointment details:\n").append(line);
        text.append(String.format("%-5s %-26s %-26s %-26s %-8s %-10s %-5s%n",
                "ID", "Customer", "Counter Staff", "Technician", "Service", "Status", "Paid"));
        text.append(line);
        for (Appointment appointment : appointments) {
            String paid = "No";
            if (DataStore.payments().findByAppointment(appointment.getAppointmentId()) != null) {
                paid = "Yes";
            }
            text.append(String.format("%-5s %-26s %-26s %-26s %-8s %-10s %-5s%n",
                    appointment.getAppointmentId(),
                    DataStore.users().idAndName(appointment.getCustomerId()),
                    DataStore.users().idAndName(appointment.getCounterStaffId()),
                    DataStore.users().idAndName(appointment.getTechnicianId()),
                    appointment.getServiceType().name(), appointment.getStatus(), paid));
        }
        text.append(line);
        return text.toString();
    }

    public LocalDate getGeneratedOn() {
        return generatedOn;
    }
}
