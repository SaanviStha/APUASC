package util;

import exception.ValidationException;
import model.User;
import storage.DataStore;

public class ValidationUtil {

    private static final String EMAIL_PATTERN = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";
    private static final String PHONE_PATTERN = "^\\d{10}$";             // exactly 10 digits
    private static final String USERNAME_PATTERN = "^[A-Za-z][A-Za-z0-9_]{3,19}$";
    private static final String NAME_PATTERN = "^[A-Za-z][A-Za-z .'@/-]{1,49}$";
    private static final String PLATE_PATTERN = "^[A-Za-z]{1,3} ?\\d{1,4} ?[A-Za-z]?$";

    // Returns true if the text is empty or only spaces
    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // Empty field check, e.g. "Email is required."
    public static void required(String label, String value) throws ValidationException {
        if (isBlank(value)) {
            throw new ValidationException(label + " is required.");
        }
    }

    // Removes line breaks so text can be stored on one line
    public static String clean(String s) {
        if (s == null) {
            return "";
        }
        return s.trim().replaceAll("\\s+", " ");   // \\s+ means one or more spaces or line breaks
    }

    // =========================
    // PERSON DETAILS
    // =========================

    // Checks name, email, phone and password
    public static void validatePerson(String name, String email, String phone, String password)
            throws ValidationException {
        required("Full name", name);
        if (!name.trim().matches(NAME_PATTERN)) {
            throw new ValidationException("Full name must be 2-50 letters (spaces, . ' - / @ allowed).");
        }
        required("Email", email);
        if (!email.trim().matches(EMAIL_PATTERN)) {
            throw new ValidationException("Please enter a valid email address, e.g. ali@mail.com");
        }
        required("Phone number", phone);
        if (!phone.trim().matches("\\d+")) {
            throw new ValidationException("Phone number must contain digits only.");
        }
        if (!phone.trim().matches(PHONE_PATTERN)) {
            throw new ValidationException("Phone number must be exactly 10 digits, e.g. 9812345678");
        }
        validatePassword(password);
    }

    // Checks the password rules
    public static void validatePassword(String password) throws ValidationException {
        required("Password", password);
        if (password.contains(" ") || password.contains("|")) {
            throw new ValidationException("Password cannot contain spaces or the '|' character.");
        }
        if (password.length() < 6 || password.length() > 30) {
            throw new ValidationException("Password must be 6-30 characters long.");
        }
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            throw new ValidationException("Password must contain at least one letter and one number.");
        }
    }

    // Checks all details of a new account
    public static void validateNewUser(User user) throws ValidationException {
        validateUser(user, null);
    }

    // Checks all details of an account that is being updated
    public static void validateExistingUser(User user) throws ValidationException {
        validateUser(user, user.getUserId());
    }

    // Shared checks for new and existing accounts
    private static void validateUser(User user, String exceptId) throws ValidationException {
        required("Username", user.getUsername());
        String username = user.getUsername().trim();
        if (!username.matches(USERNAME_PATTERN)) {
            throw new ValidationException(
                    "Username must be 4-20 characters, start with a letter and use only letters, numbers or _.");
        }
        if (DataStore.users().usernameTaken(username, exceptId)) {
            throw new ValidationException("Username '" + username + "' is already taken.");
        }
        validatePerson(user.getName(), user.getEmail(), user.getPhone(), user.getPassword());
        user.setUsername(username);
        user.setName(user.getName().trim());
        user.setEmail(user.getEmail().trim());
        user.setPhone(user.getPhone().trim());
    }

    // Checks the car model and car plate
    public static void validateCar(String carModel, String carPlate) throws ValidationException {
        required("Car model", carModel);
        if (carModel.trim().length() > 40 || carModel.contains("|")) {
            throw new ValidationException("Car model must be at most 40 characters, e.g. Perodua Myvi");
        }
        required("Car plate", carPlate);
        if (!carPlate.trim().matches(PLATE_PATTERN)) {
            throw new ValidationException("Please enter a valid car plate number, e.g. WXY 1234");
        }
    }

    // =========================
    // TEXT AND NUMBERS
    // =========================

    // Checks a comment, feedback or description
    public static void validateText(String label, String text, int min, int max) throws ValidationException {
        required(label, text);
        String t = clean(text);
        if (t.length() < min || t.length() > max) {
            throw new ValidationException(label + " must be " + min + "-" + max + " characters long.");
        }
        if (t.contains("|")) {
            throw new ValidationException(label + " cannot contain the '|' character.");
        }
    }

    // Turns text into a number, with a clear message if it is not a number
    public static double parseAmount(String label, String text) throws ValidationException {
        required(label, text);
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(label + " must be a number, e.g. 120.50");
        }
    }
}
