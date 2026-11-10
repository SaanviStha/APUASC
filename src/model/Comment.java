package model;

import util.DateUtil;

import java.time.LocalDateTime;

public class Comment implements Storable {

    private String commentId;
    private String appointmentId;
    private String customerId;
    private String targetStaffId;
    private String text;
    private LocalDateTime createdAt;

    public Comment(String commentId, String appointmentId, String customerId,
                   String targetStaffId, String text, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.targetStaffId = targetStaffId;
        this.text = text;
        this.createdAt = createdAt;
    }

    @Override
    public String getId() {
        return commentId;
    }

    // Turns the comment into one line of text for comments.txt
    @Override
    public String toFileLine() {
        return String.join(DELIMITER, commentId, appointmentId, customerId, targetStaffId, text,
                DateUtil.format(createdAt));
    }

    public String getCommentId() {
        return commentId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getTargetStaffId() {
        return targetStaffId;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
