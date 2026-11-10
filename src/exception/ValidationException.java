package exception;

/**
 * Thrown when user input or a business rule is not satisfied.
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}
