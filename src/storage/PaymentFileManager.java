package storage;

import model.Payment;
import util.DateUtil;

public class PaymentFileManager extends FileManager<Payment> {

    public PaymentFileManager() {
        super("data/payments.txt");
    }

    // paymentId|appointmentId|counterStaffId|amount|method|paidAt
    @Override
    protected Payment parseLine(String line) {
        String[] parts = split(line);
        return new Payment(parts[0], parts[1], parts[2], Double.parseDouble(parts[3]), parts[4], DateUtil.parse(parts[5]));
    }

    // Returns the payment of one appointment, or null
    public Payment findByAppointment(String appointmentId) {
        for (Payment payment : readAll()) {
            if (payment.getAppointmentId().equals(appointmentId)) {
                return payment;
            }
        }
        return null;
    }
}
