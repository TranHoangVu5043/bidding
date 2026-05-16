package Server.exception;

/**
 * Thrown when input validation fails (blank fields, bad format, etc.)
 * Maps to HTTP 400.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
