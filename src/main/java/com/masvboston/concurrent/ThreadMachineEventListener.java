package com.masvboston.concurrent;

/**
 * Event listener interface for intercepting activity broadcast by the Thread
 * Machine. It is vitaly important that that any implementation of the methods
 * of this interface do not throw an exception unless it is understood the
 * consequence of doing so on your particular usage of the Thread Machine. So in
 * general, don't throw any exceptions from these methods. Always have catch
 * blocks and handle errors, especially the ones you don't know about.
 * 
 * @author Mark Miller
 * 
 */
public interface ThreadMachineEventListener {

	/**
	 * Occurs prior to creating a pool.
	 * 
	 * @param poolName
	 *            The pool name to be created.
	 */
	void beforeCreatePool(String poolName);


	/**
	 * Occurs after creating a pool.
	 * 
	 * @param poolName
	 *            The pool name.
	 */
	void afterCreatePool(String poolName);


	/**
	 * Occurs when obtaining a pool to execute the task in an thread.
	 * 
	 * @param poolName
	 *            The pool
	 */
	void onGetPool(String poolName);

}
