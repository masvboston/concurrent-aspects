package com.masvboston.concurrent;

/**
 * Basic default implementation of the {@link ThreadEventListener} interface.
 * 
 * @author Mark Miller
 * 
 */
public class DefaultThreadEventListener implements ThreadEventListener {

	/**
	 * Just returns true.
	 */
	@Override
	public boolean beforeThread(final Runnable runnable) {

		return true;
	}


	/**
	 * Just returns.
	 */
	@Override
	public void afterThread(final Runnable runnable) {

		return;
	}


	/**
	 * Prints the strack trace of the given event and returns the error back to
	 * the thread machine to pass back to default error handler for the pool.
	 */
	@Override
	public Throwable onException(final Runnable runnable, final Throwable error) {

		// Pass the error backup so the thread pool has a chance to
		// do something with it.
		error.printStackTrace();
		return error;
	}

}
