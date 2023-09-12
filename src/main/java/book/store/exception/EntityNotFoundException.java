package book.store.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message, Throwable ex) {
        super(message, ex);
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
