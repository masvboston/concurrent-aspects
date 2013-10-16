package com.masvboston.common.util;

/**
 * Utility class for communicating time out errors.
 * 
 * @author Mark Miller, www.masvboston.com
 * 
 */
public class TimeOutRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;


	public TimeOutRuntimeException() {
		super();
	}


	public TimeOutRuntimeException(final String message) {
		super(message);
	}


	public TimeOutRuntimeException(final Throwable cause) {
		super(cause);
	}


	public TimeOutRuntimeException(final String message, final Throwable cause) {
		super(message, cause);
	}


	public TimeOutRuntimeException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
