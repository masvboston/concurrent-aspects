package com.masvboston.concurrent;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Interface for managing execution of threads in groups.
 * 
 * @see ThreadGroupLatchImpl
 * @author Mark Miller
 * 
 */
public interface ThreadGroupLatch {

	/**
	 * Checks to see if there is a thread group for the current thread context.
	 * 
	 * @return True if there is a thread group, false if there are no groups at
	 *         all.
	 */
	boolean hasThreadGroups();


	/**
	 * Blocks until all threads in threadgroup are done executing, using default
	 * timeout values for the wait.
	 * 
	 * @see ThreadGroupLatchImpl#DEFAULT_TIME_OUT_VALUE
	 * @see ThreadGroupLatchImpl#DEFAULT_TIME_UNIT
	 * 
	 * @return Number of threads successfully completing.
	 * @throws TimeoutException
	 *             The timeout expired before a thread finished.
	 */
	int waitForThreadsToFinish() throws TimeoutException;


	/**
	 * Blocks until all threads in threadgroup are done executing, using given
	 * timeout values for the wait.
	 * 
	 * @param timeOutValue
	 *            The amount of time to wait.
	 * @param timeOutValueUnit
	 *            The unit of measure for the amount of time.
	 * @return Number of threads successfully completing.
	 * @throws TimeoutException
	 *             The timeout expired before a thread finished.
	 */
	int waitForThreadsToFinish(long timeOutValue, TimeUnit timeOutValueUnit)
			throws TimeoutException;


	/**
	 * /** Creates an additional thread group for the currently active thread
	 * context.
	 * 
	 */
	void createThreadGroup();


	/**
	 * Add the current thread task to thread group.
	 * 
	 * @param future
	 *            The future task to monitor.
	 */
	void addThreadToGroup(Future<?> future);


	/**
	 * Releases reference to all tasks. This routine deconstructs the reference
	 * data structure. If external code has a direct refernce to these internal
	 * structures then they will experience the deconstruction as well.
	 * 
	 * @return The number of tasks released.
	 */
	int releaseAll();


	/**
	 * Provides the number of thread groups currently live for the current
	 * context.
	 * 
	 * @return The number of thread groups, zero if none.
	 */
	int numberOfThreadGroups();


	/**
	 * Provides the total number of threads in all groups.
	 * 
	 * @return Total number of threads
	 */
	int numberOfThreads();

}
