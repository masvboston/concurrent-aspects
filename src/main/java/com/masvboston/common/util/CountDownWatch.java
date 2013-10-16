package com.masvboston.common.util;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;

/**
 * Utility class that provides ability to set a countdown value and check to see
 * if the countdown is finished. This is not a timer per se since it does not
 * use a timing thread but merely performs the necessary calculations when
 * methods are invoked.
 * <p/>
 * 
 * Usage <br/>
 * 
 * <pre>
 * <code>
 *   CountDownWatch tw = new CountDownWatch(1, TimeUnit.SECONDS);
 *   tw.start();
 *   // do stuff
 *   tw.stop();
 * 
 *   if( tw.isTimedOut()){
 *     ...
 *   }
 * </code>
 * </pre>
 * <p/>
 * <strong>Note</strong><br/>
 * {@link #isTimedOut()} and {@link #getTime()} continue to function even if the
 * CountDownWatch is not stopped and if the current time is beyond the timeout
 * valued given.
 * 
 * 
 * @author Mark Miller, www.masvboston.com
 * 
 */
public class CountDownWatch {

	/**
	 * {@value}
	 */
	private static final String ERROR_BAD_TIME = "Value must be greater than 1";

	/**
	 * {@value}
	 */
	private static final String TIME_UNIT_BAD = "Time unit cannot be null";

	/**
	 * The duration to wait for the timeout to expire.
	 */
	private final long timeout;

	/**
	 * Stop-watch used to track time.
	 */
	private final StopWatch stopWatch = new StopWatch();


	/**
	 * Sets the initial timeout value and the time unit for that value.
	 * 
	 * @param duration
	 *            The time to wait before flagging timeout.
	 * @param timeUnit
	 *            The time unit for the value.
	 */
	public CountDownWatch(final long duration, final TimeUnit timeUnit) {

		ValidationUtils.checkNull(timeUnit, TIME_UNIT_BAD);
		ValidationUtils.checkRange(duration, 1L, null, ERROR_BAD_TIME);

		this.timeout = timeUnit.toMillis(duration);

	}


	/**
	 * Starts a new timing sessions clearing any previous time information.
	 */
	public void start() {

		this.stopWatch.start();
	}


	/**
	 * Stops the timeout clock.
	 */
	public void stop() {

		this.stopWatch.stop();
	}


	/**
	 * Retrieve the time that's left or the last time difference measured after
	 * a stopped is issued.
	 * 
	 * @return Time that's left in milliseconds. Value will be negative if time
	 *         is not stopped and timeout has expired. This negative value is
	 *         the time beyond the timeout value.
	 */
	public long getTime() {

		return this.timeout - this.stopWatch.getTime();
	}


	/**
	 * Indicates if the current time is out. If the timer is stopped but greater
	 * than the timeout, this value will return false.
	 * 
	 * @return True if timed out, false otherwise, even if stopped.
	 */
	public boolean isTimedOut() {

		return getTime() < 1;
	}

}
