package com.innowise.task.exception;

public class ValidationException extends ServiceException{

    public ValidationException() {
        super();
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(Throwable throwable) {
        super(throwable);
    }

    public ValidationException(String message, Throwable throwable) {
        super(throwable, message);
    }
}
