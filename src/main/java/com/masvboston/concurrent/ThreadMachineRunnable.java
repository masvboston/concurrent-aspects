package com.masvboston.concurrent;

import com.masvboston.concurrent.error.ThreadException;

/**
 * Runnable that the thread machinery wraps around a given runnable instance.
 * This class provides event notifications for {@link ThreadEventListener}
 * instances and also wraps the given runnable with additional error handling
 * code. All exceptions that exit from this class are of type
 * {@link ThreadException} or a subclass. This is done to help see that an
 * exception is from a thread instead of a random exception inserted into the
 * error message stream by something else (assuming you don't have good
 * logging).
 * 
 * @author Mark Miller
 * 
 */
public class ThreadMachineRunnable implements Runnable {

	/**
	 * Reference to event handler for all thread events.
	 */
	private final ThreadEventListener threadEvent;

	/**
	 * The runnable to execute.
	 */
	private final Runnable targetRunnable;


	/**
	 * Wraps the given runnable with an event dispatcher and error handler code.
	 * 
	 * @param runnable
	 *            Cannot be null.
	 */
	public ThreadMachineRunnable(final Runnable runnable, final ThreadEventListener eventListener) {

		assert null != runnable;
		assert null != eventListener;

		this.targetRunnable = runnable;
		this.threadEvent = eventListener;

	}


	@Override
	public void run() {

		if (null != this.threadEvent) {
			if (!this.threadEvent.beforeThread(this)) {
				// Event indicates not to move forward, so exit.
				return;
			}
		}

		try {
			this.targetRunnable.run();
		}
		// Since this is wrapping around an aspect proceed method, there is
		// possibility that a checked exception will bubble out.
		catch (Exception e) {
			if (null != this.threadEvent) {
				// Null indicates that error handling should stop here.
				Throwable ex = this.threadEvent.onException(this, e);

				if (null != ex) {
					// Not null so process the exception.
					if (ex instanceof ThreadException) {
						// Exception already an instance of Thread machine
						// exception so no need to wrap it.
						throw (ThreadException) e;
					}
					else {
						throw new ThreadException(ex);
					}
				}
			}
		}

		if (null != this.threadEvent) {
			this.threadEvent.afterThread(this);
		}
	}
}
