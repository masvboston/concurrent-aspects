package com.masvboston.common.util;

/**
 * Factory class for managing the creation of various controllers. This class is
 * used to manage their lifetimes as well as providing additional configuration
 * options.
 * 
 * @author Mark Miller, www.masvboston.com
 * 
 */
public class ControllerFactory {

	/**
	 * String used as monitor for managing lazy init of Run Once controller: * *
	 * {@value}
	 */
	private static final String RUN_ONCE_LOCK = "RUN ONCE LOCK MONITOR";

	/**
	 * String used as a monitor for managing lazy init of Run Once controller: *
	 * * {@value}
	 */
	private static final String RUN_TIMER_LOCK = "RUN TIMER LOCK MONTIRO";

	/**
	 * Reference to {@link RunOnceController} instance.
	 */
	private static RunOnceController runOnceController;

	/**
	 * Reference to {@link RunsOnTimerController} instance.
	 */
	private static RunsOnTimerController runOnTimerController;


	/**
	 * Creates an instance of the {@link RunOnceController} and returns it. At
	 * present this is a singleton instance.
	 * 
	 * @return An instance, never returns null.
	 */
	public static RunOnceController createRunOnceExectuionController() {

		if (null == runOnceController) {
			synchronized (RUN_ONCE_LOCK) {
				if (null == runOnceController) {
					runOnceController = new RunOnceController();
				}
			}
		}

		return runOnceController;
	}


	/**
	 * Creates an instance of the {@link RunsOnTimerController} and returns it.
	 * At present this is a singleton instance.
	 * 
	 * @return An instance, never returns null.
	 */
	public static RunsOnTimerController createRunOnTimerController() {

		if (null == runOnTimerController) {
			synchronized (RUN_TIMER_LOCK) {
				if (null == runOnTimerController) {
					runOnTimerController = new RunsOnTimerController();
				}
			}
		}

		return runOnTimerController;
	}

}
