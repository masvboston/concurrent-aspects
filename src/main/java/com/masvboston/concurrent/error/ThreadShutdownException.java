package com.masvboston.concurrent.error;

/**
 * Exception specifically to communicate shutdown request.
 * 
 * @author Mark Miller
 * 
 */
public class ThreadShutdownException extends ThreadException {
    private static final long serialVersionUID = 1L;


    public ThreadShutdownException() {
        super();
    }


    public ThreadShutdownException(final String message, final Throwable cause) {
        super(message, cause);
    }


    public ThreadShutdownException(final String message) {
        super(message);
    }


    public ThreadShutdownException(final Throwable cause) {
        super(cause);
    }

}
