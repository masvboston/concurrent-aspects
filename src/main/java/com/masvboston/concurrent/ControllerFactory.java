package com.masvboston.concurrent;


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
	 * String used as a monitor for managing lazy init of Thread Machine
	 * Controller: {@value}
	 */
	private static final String TMACHINE_LOCK = "THREAD MACHINE CONTROLLER LOCK MONITOR";

	/**
	 * Reference to {@link ThreadMachineController} instance.
	 */
	private static ThreadMachineController threadMachineController;


	/**
	 * Creates an instance of the {@link ThreadMachineController} and returns
	 * it. At present this is a singleton instance.
	 * 
	 * @return An instance, never returns null.
	 */
	public static ThreadMachineController createThreadMachineController() {

		if (null == threadMachineController) {
			synchronized (TMACHINE_LOCK) {
				if (null == threadMachineController) {
					threadMachineController = new ThreadMachineController();
				}
			}
		}

		return threadMachineController;
	}

}
