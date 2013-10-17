package com.masvboston.concurrent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.masvboston.common.util.ValidationUtils;
import com.masvboston.concurrent.error.ThreadException;

/**
 * Class that encapsulates application wide thread management. This class
 * facilitates threading behavior applied for an associate aspect library.
 * Provides central management of threads from thread pools, thread shutdown and
 * incorporation into shutdown hooks.
 * 
 * 
 * @author Mark Miller
 * 
 */
public class ThreadMachineController {

	/**
	 * {@value}
	 */
	private static final String ERROR_SHUTDOWN_HOOK_SET =
			"There was a problem setting the shutdown hook";

	/**
	 * {@value}
	 */
	private static final String ERROR_BAD_RESET = "Cannot reset until shutdown issued first";

	/**
	 * {@value}
	 */
	private static final String ERROR_POOL_NAME = "Pool name cannot be empty or null";

	/**
	 * {@value}
	 */
	private static final String ERROR_IN_SHUTDOWN = "Cannot comply, thread machine shutdown";

	/**
	 * {@value}
	 */
	private static final String ERROR_NULL_RUNNABLE = "Runnable cannot be null";

	/**
	 * {@value}
	 */
	private static final String ERROR_TIME_UNIT = "Time unit cannot be null";

	/**
	 * {@value}
	 */
	private static final String ERROR_WAIT_TIME = "Wait time cannot be less than 1";

	/**
	 * Error message for when thread groups stops waiting for work to complete.
	 */
	private static final String ERROR_TIME_OUT =
			"Timed out waiting for threads to complete, time in minutes is:  ";

	/**
	 * Flag to use to check for shutdown status.
	 */
	private volatile boolean shutdown = false;

	/**
	 * Reference to unbound thread executor ID for lookup of the unbounded
	 * thread executor.
	 */
	private final String UNBOUND_EXECUTOR_ID = UUID.randomUUID().toString();

	/**
	 * Keeps a Map of thread pools by their unique ID.
	 */
	private Map<String, ExecutorService> executors = initExecutorService();

	/**
	 * The instance of the thread group latch factory to use for managing
	 * execution of groups of threads.
	 */
	private final ThreadGroupLatch threadGroupLatch = ThreadGroupLatchFactory
			.createThreadGroupLatch();

	/**
	 * Default event handler for thread events.
	 */
	private final ThreadEventListener DEFAULT_THREAD_EVENT_LISTENER =
			new DefaultThreadEventListener();

	/**
	 * Default event handler for thread machine events.
	 */
	private final ThreadMachineEventListener DEFAULT_THREAD_MACHINE_HANDLER =
			new DefaultThreadMachineEventListener();

	/**
	 * The current event handler for thread machine events.
	 */
	private volatile ThreadMachineEventListener threadMachineEventListener =
			this.DEFAULT_THREAD_MACHINE_HANDLER;

	/**
	 * The current thread event listener.
	 */
	private volatile ThreadEventListener threadEventListener = this.DEFAULT_THREAD_EVENT_LISTENER;


	/**
	 * Creates the default executors.
	 * 
	 * @return Map of default executors.
	 */
	protected Map<String, ExecutorService> initExecutorService() {

		ConcurrentHashMap<String, ExecutorService> ex =
				new ConcurrentHashMap<String, ExecutorService>();
		ex.put(this.UNBOUND_EXECUTOR_ID, ExecutorServiceFactory.createCachedExecutorService());
		return ex;
	}


	/**
	 * Gets the event handler for thread machine events.
	 * 
	 * @return The handler, never null.
	 */
	public ThreadMachineEventListener getThreadMachineEventListener() {

		return this.threadMachineEventListener;
	}


	/**
	 * Sets the thread event handler for the thread machine.
	 * 
	 * @param eventHandler
	 *            THe event handler to use. Pass in null to reset to default
	 *            event handler.
	 */
	public void setThreadMachineEventListener(final ThreadMachineEventListener eventHandler) {

		this.threadMachineEventListener =
				null == eventHandler ? this.DEFAULT_THREAD_MACHINE_HANDLER : eventHandler;
	}


	/**
	 * Retrieves an Executor stored with the given pool name. If one does not
	 * exist under the given pool name it is created.
	 * 
	 * @param poolName
	 *            The ID under which the pool is stored.
	 * @return The executor stored under the given ID. Never returns null.
	 */
	protected ExecutorService getExecutorService(final String poolName) {

		ValidationUtils.checkNull(poolName, ERROR_POOL_NAME);
		ValidationUtils.checkEmpty(poolName, ERROR_POOL_NAME);

		ExecutorService svc = this.executors.get(poolName);

		if (null == svc) {

			synchronized (this.executors) {

				// Check again just in case the pool was created already.
				svc = this.executors.get(poolName);

				if (null == svc) {
					// No pool, so create one.
					svc = ExecutorServiceFactory.createExecutorService();
					this.executors.put(poolName, svc);
				}
			}
		}

		return svc;
	}


	/**
	 * Get the current event listener. If you want the ability to register more
	 * than one listener, best to implement a listener interface that manages a
	 * list of other registrants and broadcasts the in-bound message to them.
	 * 
	 * @return The current event listener or null if none is set.
	 */
	public ThreadEventListener getThreadEventListener() {

		return this.threadEventListener;
	}


	/**
	 * Set the thread event listener that is used to broadcast messages for all
	 * thread events. Passing in null will reset the listener to the default
	 * listener.
	 * 
	 * @param eventListener
	 *            A new listener or null to reset the listener to use the
	 *            default.
	 */
	public void setThreadEventListener(final ThreadEventListener eventListener) {

		this.threadEventListener =
				null == eventListener ? this.DEFAULT_THREAD_EVENT_LISTENER : eventListener;
	}


	/**
	 * Adding shutdown hook to ensure Threads are forced down when JVM
	 * terminates.
	 */
	public ThreadMachineController() {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				try {

					shutdown(10, TimeUnit.SECONDS);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		Thread hook = new Thread(runnable);

		try {
			Runtime.getRuntime().addShutdownHook(hook);
		}
		catch (Exception e) {
			// Make sure we see the error.
			e.printStackTrace();
			System.out.println(ERROR_SHUTDOWN_HOOK_SET);
			throw new RuntimeException(ERROR_SHUTDOWN_HOOK_SET, e);
		}
	}


	/**
	 * Execute after a shutdown request to reset the class to be used again.
	 */
	public void reset() {

		synchronized (this.executors) {

			if (!isShutdown()) {
				throw new IllegalStateException(ERROR_BAD_RESET);
			}

			this.shutdown = false;

			this.executors = initExecutorService();
		}

	}


	/**
	 * Executes the given runnable in a thread from a bounded or unbounded pool
	 * as determined by the input parameters.
	 * 
	 * @param poolable
	 *            Flag indicating if task should run in a thread from a bounded
	 *            pool; true if so, false if task should run from an unbounded
	 *            pool.
	 * @param poolName
	 *            The name of the pool to use, cannot be null or empty.
	 * @param groupable
	 *            Indicates if the thread should be part of a thread group
	 * @param runnable
	 *            The task to run.
	 */
	public void executeInThread(final boolean poolable, final String poolName,
			final boolean groupable, Runnable runnable) {

		checkForShutdown();

		/*
		 * If poolable then the poolname is provided, if not poolable then
		 * default to the unbound thread pool.
		 */
		ExecutorService svc =
				poolable ? getExecutorService(poolName)
						: getExecutorService(this.UNBOUND_EXECUTOR_ID);

				/*
				 * Wrap the event dispatcher and error management wrapper around the
				 * given runnable.
				 */
				runnable = new ThreadMachineRunnable(runnable, getThreadEventListener());

				/*
				 * Wrap a future around the runnable so it can be passed on to thread
				 * group latching mechanism. Have to do this so that thread finish,
				 * cancellation, and exception handling behavior expected from a future
				 * works properly.
				 */
				RunnableFuture<Boolean> rf = createRunnableFuture(runnable);

				// Execute the task.
				svc.execute(rf);

				// Only add to latch group if marked as groupable.
				if (groupable) {

					/*
					 * Check to see if a thread group latch has been setup for this
					 * thread context, and if so then this future must be managed by it.
					 * If no thread group latch was setup than this thread just runs
					 * free with nothing waiting on it finishing.
					 */
					if (this.threadGroupLatch.hasThreadGroups()) {
						this.threadGroupLatch.addThreadToGroup(rf);
					}
				}

	}


	/**
	 * Utility method, checks for shutdown and throws an exception if shutdown
	 * was invoked.
	 */
	private void checkForShutdown() {

		if (isShutdown()) {
			throw new IllegalStateException(ERROR_IN_SHUTDOWN);
		}
	}


	/**
	 * Setup the thread group. This is automatically assigned to the current
	 * thread context. Care must be taken to ensure that the thread group is
	 * cleaned up when the method unwinds.
	 */
	public void createThreadGroup() {

		checkForShutdown();
		this.threadGroupLatch.createThreadGroup();
	}


	/**
	 * Method used to wait on all threads in the current thread group to finish.
	 * The current thread group is the thread group created for the current
	 * thread context.
	 * 
	 * @param timeOut
	 *            Time to wait before returning regardless of the finished state
	 *            of the request.
	 * @param timeUnit
	 *            The unit of time.
	 * @throws ThreadException
	 *             Timed out while waiting.
	 */
	public void awaitCurrentThreadGroup(final long timeOut, final TimeUnit timeUnit) {

		try {
			this.threadGroupLatch.waitForThreadsToFinish(timeOut, timeUnit);
		}
		catch (TimeoutException e) {
			throw new ThreadException(ERROR_TIME_OUT + timeOut, e);
		}
	}


	/**
	 * Utility class that creates a future task that also implements Runnable .
	 * 
	 * @param runnable
	 *            A runnable to execute. Cannot be null.
	 * @return An instance of a future that also implements Runnable. This
	 *         method never returns null.
	 */
	private RunnableFuture<Boolean> createRunnableFuture(final Runnable runnable) {

		ValidationUtils.checkNull(runnable, ERROR_NULL_RUNNABLE);

		FutureTask<Boolean> future = new FutureTask<Boolean>(runnable, Boolean.TRUE);

		return future;
	}


	/**
	 * Checks shutdown flag.
	 * 
	 * @return True if all thread executors shutdown false otherwise.
	 */
	public boolean isShutdown() {

		return this.shutdown;
	}


	/**
	 * Signals factory to shutdown all threads under its control. Method waits
	 * for threads to shutdown and if they fail to shutdown within the given
	 * time limit, a forced shutdown occurs.
	 * 
	 * @param waitTime
	 *            The number of time units to wait for all threads to stop
	 *            running. Must be a value greater than 0.
	 * @param timeUnit
	 *            The Time unit to apply to the given wait time. Cannot be null.
	 */
	public void shutdown(final long waitTime, final TimeUnit timeUnit) {

		ValidationUtils.checkNull(timeUnit, ERROR_TIME_UNIT);

		/*
		 * Already shutdown so return.
		 */
		if (isShutdown()) {
			return;
		}

		/*
		 * First shutdown all the executors and then go back and make sure they
		 * are shutdown.
		 */
		synchronized (this.executors) {

			/*
			 * Check again, if already shutdown return.
			 */
			if (isShutdown()) {
				return;
			}

			if (1 > waitTime) {
				throw new IllegalArgumentException(ERROR_WAIT_TIME);
			}

			this.shutdown = true;

			/*
			 * Divide the wait-time for each thread pool, because everything has
			 * to shutdown in the given amount of (time.
			 */
			long time = timeUnit.toNanos(waitTime);
			// executor size will never be zero.
			time = (time / this.executors.size()) + 1;

			/*
			 * Tell everything to shutdown.
			 */
			for (ExecutorService svc : this.executors.values()) {
				svc.shutdown();
			}

			/*
			 * Check to see that they are all terminated and if not tell them to
			 * shutdown now.
			 */
			for (ExecutorService svc : this.executors.values()) {
				try {

					boolean terminated = svc.awaitTermination(waitTime, timeUnit);

					if (!terminated) {
						svc.shutdownNow();
					}
				}
				catch (InterruptedException e) {
					/*
					 * Just print the error because we need to go and make sure
					 * all the executors are shutdown.
					 */
					e.printStackTrace();
				}
			}

			/*
			 * Get rid of any thread group latches.
			 */
			this.threadGroupLatch.releaseAll();
		}
	}

}
