package com.masvboston.common.util;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Controller class for managing the execution of callbacks on a timer. Once the
 * instance owning the callback goes out of scope the Timer is also reclaimed
 * and the callback no longer executes.
 * 
 * @author Mark Miller, www.masvboston.com
 * 
 */
public class RunsOnTimerController {

	/**
	 * {@value}
	 */
	private static final String ERROR_BAD_DELAY = "Delay cannot be less than 0: ";

	/**
	 * {@value}
	 */
	private static final String ERROR_BAD_PERIOD = "Period cannot be less than 1: ";

	/**
	 * {@value}
	 */
	private static final String ERROR_BAD_INSTANCE = "Instance cannot be null";

	/**
	 * {@value}
	 */
	private static final String ERROR_BAD_TIME_UNIT = "Time Unit cannot be null";

	/**
	 * {@value}
	 */
	private static final String ERROR_BAD_CALLBACK = "Callback cannot be null";

	/**
	 * {@value}
	 */
	private static final String ERROR_BAD_ATTR = "The attribute value cannot be null";

	/**
	 * Keeps track of a timer associated with a callback on an instance (or
	 * class).
	 */
	private final InstanceAttributeTracker<Object, Object, Timer> timerTracker;


	/**
	 * Initialize the controller.
	 */
	public RunsOnTimerController() {
		this.timerTracker = new InstanceAttributeTracker<Object, Object, Timer>();
	}


	/**
	 * Creates a timer for the given object instance and attribute and ensures
	 * it executes per the provided settings. If a timer already exists for the
	 * object and attribute the callback is invoked directly. Note that
	 * callbacks that execute on a timer and encounter an exception have that
	 * exception output in the default error output while callbacks that already
	 * have a timer execute immediately and the exception is thrown the the
	 * caller.
	 * 
	 * @param instance
	 *            The object instance to track.
	 * @param attribute
	 *            The attribute belonging to the instance to track.
	 * @param delay
	 *            The amount of time to delay before invoking the first
	 *            callback.
	 * @param period
	 *            The interval at which to invoke the callback.
	 * @param timeUnit
	 *            The {@link TimeUnit} of the delay and interval value.
	 * @param callBack
	 *            The callback to execute on the timer.
	 * @return True if the attribute was added, false otherwise.
	 */
	public boolean add(final Object instance, final Object attribute, final long delay,
			final long period, final TimeUnit timeUnit, final Runnable callBack) {

		ValidationUtils.checkNull(callBack, ERROR_BAD_CALLBACK);
		ValidationUtils.checkNull(timeUnit, ERROR_BAD_TIME_UNIT);
		ValidationUtils.checkNull(instance, ERROR_BAD_INSTANCE);
		ValidationUtils.checkNull(attribute, ERROR_BAD_ATTR);

		if (1 > period) {
			throw new IllegalArgumentException(ERROR_BAD_PERIOD + period);
		}

		if (0 > delay) {
			throw new IllegalArgumentException(ERROR_BAD_DELAY + delay);
		}

		synchronized (this.timerTracker) {

			if (this.timerTracker.contains(instance, attribute)) {
				callBack.run();
				return false;
			}
			else {

				TimerTask timerTask = new TimerTask() {

					private final Runnable runner = callBack;


					@Override
					public void run() {
						try {
							this.runner.run();
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				};

				long theDelay = TimeUnit.MILLISECONDS.convert(delay, timeUnit);

				long thePeriod = TimeUnit.MILLISECONDS.convert(period, timeUnit);

				Timer timer = new Timer(true);
				timer.schedule(timerTask, theDelay, thePeriod);

				this.timerTracker.add(instance, attribute, timer);

				return true;
			}

		}

	}

}
