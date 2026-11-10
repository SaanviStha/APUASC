package util;

import exception.ValidationException;
import model.ServiceType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {

    public static final String PATTERN = "yyyy-MM-dd HH:mm";
    public static final LocalTime OPENING = LocalTime.of(9, 0);
    public static final LocalTime CLOSING = LocalTime.of(18, 0);

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern(PATTERN);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Turns text such as "2026-10-20 10:00" into a date-time
    public static LocalDateTime parse(String text) {
        return LocalDateTime.parse(text.trim(), DATE_TIME);
    }

    // Same as parse, but gives a clear message if the text is wrong
    public static LocalDateTime parseInput(String text) throws ValidationException {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("Date is required.");
        }
        try {
            return parse(text);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Please enter date and time as " + PATTERN + ", e.g. 2026-10-20 10:00");
        }
    }

    // Turns a date-time into text such as "2026-10-20 10:00"
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_TIME);
    }

    // Turns a date into text such as "2026-10-20"
    public static String formatDate(LocalDate date) {
        return date.format(DATE);
    }

    /**
     * Business rules: Monday to Saturday, 9am to 6pm, on the hour or half hour,
     * not in the past, at most 90 days ahead.
     */
    public static void validateBookingTime(LocalDateTime start, ServiceType type) throws ValidationException {
        if (start == null) {
            throw new ValidationException("Please choose a date and time.");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Appointment time cannot be in the past.");
        }
        if (start.isAfter(LocalDateTime.now().plusDays(90))) {
            throw new ValidationException("Appointments can only be booked up to 90 days ahead.");
        }
        if (start.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new ValidationException("The service centre is closed on Sundays.");
        }
        if (start.getMinute() % 30 != 0) {
            throw new ValidationException("Appointments start on the hour or half hour (e.g. 10:00 or 10:30).");
        }
        LocalTime begin = start.toLocalTime();
        LocalTime end = begin.plusHours(type.getHours());
        if (begin.isBefore(OPENING) || end.isAfter(CLOSING) || end.isBefore(begin)) {
            throw new ValidationException("A " + type.getDisplayName().toLowerCase()
                    + " must fit within opening hours (09:00 - 18:00).");
        }
    }
}
