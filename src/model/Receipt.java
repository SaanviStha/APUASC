package model;

import storage.DataStore;
import util.DateUtil;

import java.time.LocalDateTime;

public class Receipt {

    private final String receiptNo;
    private final String paymentId;
    private final LocalDateTime issuedAt;

    private final Payment payment;
    private final Appointment appointment;

    public Receipt(Payment payment, Appointment appointment) {
        this.payment = payment;
        this.appointment = appointment;
        this.paymentId = payment.getPaymentId();
        this.receiptNo = "RCP-" + payment.getPaymentId();
        this.issuedAt = payment.getPaidAt();
    }

    // Builds the text of the receipt
    public String format() {
        User customer = DataStore.users().findById(appointment.getCustomerId());
        User staff = DataStore.users().findById(payment.getCounterStaffId());
        User tech = DataStore.users().findById(appointment.getTechnicianId());

        String line = "==============================================\n";
        StringBuilder text = new StringBuilder();
        text.append(line);
        text.append("        APU AUTOMOTIVE SERVICE CENTRE\n");
        text.append("             OFFICIAL RECEIPT\n");
        text.append(line);
        text.append(String.format("Receipt No   : %s%n", receiptNo));
        text.append(String.format("Payment ID   : %s%n", paymentId));
        text.append(String.format("Date Issued  : %s%n", DateUtil.format(issuedAt)));
        text.append("----------------------------------------------\n");
        text.append(String.format("Customer     : %s%n", nameOf(customer, appointment.getCustomerId())));
        if (customer instanceof Customer) {
            Customer carOwner = (Customer) customer;
            text.append(String.format("Vehicle      : %s (%s)%n", carOwner.getCarModel(), carOwner.getCarPlate()));
        }
        text.append(String.format("Appointment  : %s%n", appointment.getAppointmentId()));
        text.append(String.format("Service      : %s%n", appointment.getServiceType()));
        text.append(String.format("Service Date : %s%n", DateUtil.format(appointment.getStartTime())));
        text.append(String.format("Technician   : %s%n", nameOf(tech, appointment.getTechnicianId())));
        text.append("----------------------------------------------\n");
        text.append(String.format("Amount Paid  : RM %.2f%n", payment.getAmount()));
        text.append(String.format("Method       : %s%n", payment.getMethod()));
        text.append(String.format("Received By  : %s%n", nameOf(staff, payment.getCounterStaffId())));
        text.append(line);
        text.append("     Thank you for choosing APU-ASC!\n");
        text.append(line);
        return text.toString();
    }

    // Returns the user's name, or the ID if the user was deleted
    private String nameOf(User user, String fallbackId) {
        if (user == null) {
            return fallbackId + " (removed)";
        }
        return user.getName();
    }

    public String getReceiptNo() {
        return receiptNo;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
}
