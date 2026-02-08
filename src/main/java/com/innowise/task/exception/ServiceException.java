package com.innowise.task.exception;

import org.apache.logging.log4j.message.Message;

public abstract class ServiceException extends RuntimeException {
    public ServiceException(){
        super();
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(Throwable throwable) {
        super(throwable);
    }

    public ServiceException(Throwable throwable, String message) {
        super(message, throwable);
    }
}
