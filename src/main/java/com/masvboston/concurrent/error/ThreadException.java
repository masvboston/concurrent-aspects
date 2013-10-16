package com.masvboston.concurrent.error;

/**
 * Exception class to inidicate errors occuring in the thread machine framework.
 * 
 * @author Mark Miller
 * 
 */
public class ThreadException extends RuntimeException {
    private static final long serialVersionUID = 1L;


    public ThreadException() {
        super();
    }


    public ThreadException(final String message, final Throwable cause) {
        super(message, cause);
    }


    public ThreadException(final String message) {
        super(message);
    }


    public ThreadException(final Throwable cause) {
        super(cause);
    }

}
