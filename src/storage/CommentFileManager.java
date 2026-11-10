package storage;

import model.Comment;
import util.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class CommentFileManager extends FileManager<Comment> {

    public CommentFileManager() {
        super("data/comments.txt");
    }

    // commentId|appointmentId|customerId|targetStaffId|text|createdAt
    @Override
    protected Comment parseLine(String line) {
        String[] parts = split(line);
        return new Comment(parts[0], parts[1], parts[2], parts[3], parts[4], DateUtil.parse(parts[5]));
    }

    // Returns the comments of one appointment
    public List<Comment> findByAppointment(String appointmentId) {
        List<Comment> list = new ArrayList<>();
        for (Comment comment : readAll()) {
            if (comment.getAppointmentId().equals(appointmentId)) {
                list.add(comment);
            }
        }
        return list;
    }
}
