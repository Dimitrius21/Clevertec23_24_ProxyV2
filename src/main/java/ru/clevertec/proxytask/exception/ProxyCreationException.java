package ru.clevertec.proxytask.exception;

public class ProxyCreationException extends RuntimeException{
    public ProxyCreationException(String message) {
        super(message);
    }

    public ProxyCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProxyCreationException(Throwable cause) {
        super(cause);
    }
}
