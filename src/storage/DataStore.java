package storage;

/**
 * One shared file manager per text file.
 */
public class DataStore {

    private static final UserFileManager USERS = new UserFileManager();
    private static final AppointmentFileManager APPOINTMENTS = new AppointmentFileManager();
    private static final PaymentFileManager PAYMENTS = new PaymentFileManager();
    private static final FeedbackFileManager FEEDBACK = new FeedbackFileManager();
    private static final CommentFileManager COMMENTS = new CommentFileManager();
    private static final PriceFileManager PRICES = new PriceFileManager();

    // The file manager for users.txt
    public static UserFileManager users() {
        return USERS;
    }

    // The file manager for appointments.txt
    public static AppointmentFileManager appointments() {
        return APPOINTMENTS;
    }

    // The file manager for payments.txt
    public static PaymentFileManager payments() {
        return PAYMENTS;
    }

    // The file manager for feedback.txt
    public static FeedbackFileManager feedback() {
        return FEEDBACK;
    }

    // The file manager for comments.txt
    public static CommentFileManager comments() {
        return COMMENTS;
    }

    // The file manager for prices.txt
    public static PriceFileManager prices() {
        return PRICES;
    }
}
