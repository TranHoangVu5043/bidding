package Server.exception;

/**
 * Thrown when authentication fails (wrong credentials, expired/invalid token).
 * Maps to HTTP 401.
 */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}