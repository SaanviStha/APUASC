package model;

import util.DateUtil;

import java.time.LocalDateTime;

public class Feedback implements Storable {

    private String feedbackId;
    private String appointmentId;
    private String technicianId;
    private String text;
    private LocalDateTime createdAt;

    public Feedback(String feedbackId, String appointmentId, String technicianId,
                    String text, LocalDateTime createdAt) {
        this.feedbackId = feedbackId;
        this.appointmentId = appointmentId;
        this.technicianId = technicianId;
        this.text = text;
        this.createdAt = createdAt;
    }

    @Override
    public String getId() {
        return feedbackId;
    }

    // Turns the feedback into one line of text for feedback.txt
    @Override
    public String toFileLine() {
        return String.join(DELIMITER, feedbackId, appointmentId, technicianId, text,
                DateUtil.format(createdAt));
    }

    public String getFeedbackId() {
        return feedbackId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getTechnicianId() {
        return technicianId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
