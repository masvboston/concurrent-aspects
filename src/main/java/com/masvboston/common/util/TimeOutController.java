package com.masvboston.common.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Controls execution of a method within a timeout period. Use this controller
 * to executes methods that must complete within a given time period. If the
 * method does not complete a {@link TimeOutRuntimeException} is thrown and the
 * method execution is cancelled. <br/>
 * 
 * This class uses a cached thread pool by default. These threads are
 * <em>Daemon</em> threads by default. Use the appropriate constructor to change
 * this behavior or provide your own Executor.
 * 
 * @author Mark Miller, www.masvboston.com
 * 
 */
public class TimeOutController {

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
	private static final String ERROR_BAD_TIMEOUT = "Time out value must be greater than zero: ";

	/**
	 * The executor to manage the thread execution.
	 */
	private final Executor executor;

	/**
	 * Flag indicating if threads should be daemon threads.
	 */
	private final boolean makeDaemon;


	/**
	 * Initializes the system with default values.
	 * 
	 * @throws IllegalStateException
	 *             A null executor was provided by a subclass override of
	 *             {@link #doCreateExecutor()}.
	 */
	public TimeOutController() {
		this(true);
	}


	/**
	 * Initializes the system.
	 * 
	 * @param useDaemonThreads
	 *            Set to true to have all threads created to execute callbacks
	 *            be daemon threads, false otherwise.
	 */
	public TimeOutController(final boolean useDaemonThreads) {

		this.makeDaemon = useDaemonThreads;

		Executor ex = doCreateExecutor();

		ValidationUtils.checkNull(ex, "Executor cannot be null");

		this.executor = ex;

	}


	/**
	 * Initializes the system with the given executor.
	 * 
	 * @param ex
	 *            Cannot be null.
	 * @throws IllegalStateException
	 *             A null executor was provided.
	 */
	public TimeOutController(final Executor ex) {

		ValidationUtils.checkNull(ex, "Executor cannot be null");

		this.makeDaemon = false;

		this.executor = ex;

	}


	/**
	 * Override to change the default executor that is used by this class.
	 * 
	 * @return An executor instance, value cannot be null or an error will be
	 *         created.
	 */
	protected Executor doCreateExecutor() {

		ThreadFactory tf = new ThreadFactory() {
			ThreadFactory factory = Executors.defaultThreadFactory();

			final boolean isDaemon = TimeOutController.this.makeDaemon;


			@Override
			public Thread newThread(final Runnable runnable) {
				Thread thread = this.factory.newThread(runnable);
				thread.setDaemon(this.isDaemon);
				return thread;
			}
		};

		return Executors.newCachedThreadPool(tf);
	}


	/**
	 * Executes the given callback within the timeout period. If the timeout is
	 * exceeded a {@link TimeOutRuntimeException} is thrown. Be sure to craft
	 * your callbacks so that any looping code checks the current thread for
	 * interruption requests so that a graceful shutdown is possible; otherwise,
	 * the thread will be forced to stop abruptly.
	 * 
	 * @param callBack
	 *            The callback to execute, cannot be null.
	 * @param timeOut
	 *            The time to wait for the callback to finish.
	 * @param timeUnit
	 *            the unit of time of the timeOut value.
	 * @exception TimeOutRuntimeException
	 *                The method execution timed out.
	 */
	public void execute(final Runnable callBack, final long timeOut, final TimeUnit timeUnit) {

		ValidationUtils.checkNull(callBack, ERROR_BAD_CALLBACK);
		ValidationUtils.checkNull(timeUnit, ERROR_BAD_TIME_UNIT);

		if (1 > timeOut) {
			throw new IllegalArgumentException(ERROR_BAD_TIMEOUT + timeOut);
		}

		FutureTask<Boolean> ft = new FutureTask<Boolean>(callBack, true);

		this.executor.execute(ft);

		try {
			ft.get(timeOut, timeUnit);
		}
		catch (TimeoutException e) {
			throw new TimeOutRuntimeException(e);
		}
		catch (InterruptedException e) {
			throw new TimeOutRuntimeException(e);
		}
		catch (ExecutionException e) {
			throw new TimeOutRuntimeException(e);
		}
		finally {
			ft.cancel(true);
		}
	}
}
