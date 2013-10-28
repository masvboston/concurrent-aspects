package com.masvboston.concurrent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.masvboston.concurrent.error.ThreadException;
import com.masvboston.concurrent.error.ThreadShutdownException;

/**
 * Provides latching of thread groups, where thread groups occur within the call
 * flow of a parent thread and the parent thread waits on all threads in a group
 * to finish before moving on. This class is meant to be used in conjunction
 * with aspects that manage the creation of threads and thread groups, but it
 * can be used without these aspects provided you follow one simple but very
 * important rule:
 * 
 * <h3>You must call {@link #awaitThreadGroupDone()} for every thread group
 * created</h3>
 * 
 * <strong> Failure to do so will result in orphaned objects awaiting to be
 * collected but never are. </strong>
 * 
 * <em>As a last resort you can call {@link #releaseAll()} method to deconstruct all
 * tasks this class is holding to ensure there is no memory leak.</em>
 * 
 * 
 * 
 * <p/>
 * 
 * Muliptle thread groups can exist for the currently active thread. The active
 * thread owns the groups of threads and management of their lifetimes and
 * processing of their results occurs within the context of thread that created
 * them.
 * 
 * <p/>
 * 
 * Child threads can have their own thread groups, but since the groups are
 * created within the context of the child thread their lifetimes and output is
 * managed in that child threads context.
 * 
 * <p/>
 * 
 * Multiple thread groups created in the same thread context are managed
 * sequentially in the order in which they occur.
 * 
 * <p/>
 * This class is thread safe.
 * 
 * @author Mark Miller
 * 
 */
class ThreadGroupLatchImpl implements ThreadGroupLatch {

	/**
	 * {@value}
	 */
	public static final String ERROR_NO_GROUPS =
			"There are no thread groups for the current context.  Create a thread gorup first";

	/**
	 * {@value}
	 */
	public static final int DEFAULT_TIME_OUT_VALUE = 5;

	/**
	 * {@value}
	 */
	public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;

	/**
	 * List of threads for current thread group.
	 */
	private static class ThreadGroup extends LinkedList<Future<?>> {

		private static final long serialVersionUID = 1L;

	}

	/**
	 * Thread local storage for the thread groups
	 */
	private static final ThreadLocal<Queue<ThreadGroup>> threadGroups =
			new ThreadLocal<Queue<ThreadGroup>>();


	@Override
	public boolean hasThreadGroups() {

		Queue<ThreadGroup> groups = threadGroups.get();

		if (null == groups) {
			return false;
		}

		return (0 < groups.size());
	}


	@Override
	public int waitForThreadsToFinish() throws TimeoutException {

		return waitForThreadsToFinish(DEFAULT_TIME_OUT_VALUE, DEFAULT_TIME_UNIT);
	}


	@Override
	public int waitForThreadsToFinish(final long timeOutValue, final TimeUnit timeOutValueUnit)
			throws TimeoutException {

		Queue<ThreadGroup> groups = threadGroups.get();

		// No group queue then nothing to do.
		if (null == groups) {
			return 0;
		}

		ThreadGroup threadGroup = groups.poll();

		// If there is no group, then there is nothing to do.
		if (null == threadGroup) {
			return 0;
		}

		int numThreads = 0;

		// We have thread tasks to go through. Call get() on each one to wait
		// for it to finish. When all are finished this method returns to the
		// caller.
		for (Future<?> thread : threadGroup) {
			try {
				thread.get(timeOutValue, timeOutValueUnit);
				numThreads++;
			}
			catch (InterruptedException e) {
				throw new ThreadException(e);
			}
			catch (ExecutionException e) {
				Throwable cause = e.getCause();

				if (cause instanceof ThreadShutdownException) {
					throw (ThreadShutdownException) cause;
				}

				throw new ThreadException(e.getCause());
			}
		}

		return numThreads;
	}


	@Override
	public void createThreadGroup() {

		Queue<ThreadGroup> groups = threadGroups.get();

		// If there are no Thread groups at all then initialize it.
		if (null == groups) {

			// Need to use LIFO since last thread group added is the first one
			// you want to process when backing out through the call stack.
			groups = Collections.asLifoQueue(new LinkedList<ThreadGroup>());

			threadGroups.set(groups);
		}

		ThreadGroup group = new ThreadGroup();
		groups.add(group);
	}


	@Override
	public void addThreadToGroup(final Future<?> future) {

		if (null == future) {
			throw new IllegalArgumentException("Future task cannot be null");
		}

		Queue<ThreadGroup> groups = threadGroups.get();

		if (null == groups) {
			throw new IllegalStateException(ERROR_NO_GROUPS);
		}

		ThreadGroup group = groups.peek();

		if (null == group) {
			throw new IllegalStateException(ERROR_NO_GROUPS);
		}

		group.add(future);

	}


	@Override
	public int releaseAll() {

		// Get the queue of all groups
		Queue<ThreadGroup> groups = threadGroups.get();

		// Remove the reference of the queue form thread local storage.
		threadGroups.remove();

		// Nothing to do so return 0;
		if (null == groups) {
			return 0;
		}

		int i = 0;

		// Go through each group and count up the tasks in them and add to the
		// total.
		for (ThreadGroup group : groups) {
			i += group.size();

			// Clear all references for good measure.
			group.clear();
		}

		// Clear all references to the groups for good measure.
		groups.clear();

		// Return the total number of threads.
		return i;
	}


	@Override
	public int numberOfThreadGroups() {

		if (!hasThreadGroups()) {
			return 0;
		}

		return threadGroups.get().size();
	}


	@Override
	public int numberOfThreads() {

		if (!hasThreadGroups()) {
			return 0;
		}

		int result = 0;

		for (ThreadGroup group : threadGroups.get()) {
			result += group.size();
		}

		return result;
	}

}
