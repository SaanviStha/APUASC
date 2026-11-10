package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Additional feature: keeps an audit trail of important actions in data/activity_log.txt
 */
public class ActivityLog {

    private static final String FILE_PATH = "data/activity_log.txt";

    // Adds one line with the date and time to activity_log.txt
    public static void write(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(DateUtil.format(LocalDateTime.now()) + " | " + message);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Could not write activity log.");
        }
    }
}
