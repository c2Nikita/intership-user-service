package com.innowise.task.exception;

public class BusinessRuleException extends ServiceException {

    public BusinessRuleException() {
        super();
    }

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(Throwable throwable) {
        super(throwable);
    }

    public BusinessRuleException(Throwable throwable, String message) {
        super(throwable, message);
    }
}
