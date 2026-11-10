package model;

import exception.ValidationException;
import storage.DataStore;
import util.DateUtil;

import java.time.LocalDateTime;

public class Payment implements Storable {

    public static final String[] METHODS = {"Cash", "Credit/Debit Card", "E-Wallet", "Online Banking"};

    private String paymentId;
    private String appointmentId;
    private String counterStaffId;
    private double amount;
    private String method;
    private LocalDateTime paidAt;

    public Payment(String paymentId, String appointmentId, String counterStaffId,
                   double amount, String method, LocalDateTime paidAt) {
        this.paymentId = paymentId;
        this.appointmentId = appointmentId;
        this.counterStaffId = counterStaffId;
        this.amount = amount;
        this.method = method;
        this.paidAt = paidAt;
    }

    // A receipt cannot exist without its payment (composition)
    public Receipt generateReceipt() throws ValidationException {
        Appointment appointment = DataStore.appointments().findById(appointmentId);
        if (appointment == null) {
            throw new ValidationException("The appointment for this payment no longer exists.");
        }
        return new Receipt(this, appointment);
    }

    @Override
    public String getId() {
        return paymentId;
    }

    // Turns the payment into one line of text for payments.txt
    @Override
    public String toFileLine() {
        return String.join(DELIMITER, paymentId, appointmentId, counterStaffId,
                String.format("%.2f", amount), method, DateUtil.format(paidAt));
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getCounterStaffId() {
        return counterStaffId;
    }

    public double getAmount() {
        return amount;
    }

    public String getMethod() {
        return method;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }
}
