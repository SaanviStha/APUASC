package storage;

import model.Feedback;
import util.DateUtil;

public class FeedbackFileManager extends FileManager<Feedback> {

    public FeedbackFileManager() {
        super("data/feedback.txt");
    }

    // feedbackId|appointmentId|technicianId|text|createdAt
    @Override
    protected Feedback parseLine(String line) {
        String[] parts = split(line);
        return new Feedback(parts[0], parts[1], parts[2], parts[3], DateUtil.parse(parts[4]));
    }

    // Returns the feedback of one appointment, or null
    public Feedback findByAppointment(String appointmentId) {
        for (Feedback feedback : readAll()) {
            if (feedback.getAppointmentId().equals(appointmentId)) {
                return feedback;
            }
        }
        return null;
    }
}
