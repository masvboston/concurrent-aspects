package com.masvboston.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import com.masvboston.common.util.ValidationUtils;
import com.masvboston.common.util.RollingIdGenerator;


/**
 * Creates executors to be used for concurrent processing.
 * 
 * @author Mark Miller
 * 
 */
public class ExecutorServiceFactory {

	/**
	 * Number of threads to use when queue is not full: {@value}
	 */
	public static final int DEFAULT_INIT_THREADS = 5;

	/**
	 * Time in seconds to keep threads alive: {@value}
	 */
	public static final int DEFAULT_KEEP_ALIVE_TIME = 60;

	/**
	 * Max number of tasks to hold in the queue before pushing work back to
	 * caller: {@value}
	 */
	public static final int DEFAULT_QUEUE_SIZE = 100;

	/**
	 * The default naming template used when naming pools and threads. * * * * *
	 * * {@value}
	 */
	private static final String THREAD_POOL_NAME_TEMPLATE = "poolID-%1s-threadID-%2s";

	/**
	 * {@value}
	 */
	private static final String UBPOOL = "unbounded" + THREAD_POOL_NAME_TEMPLATE;

	/**
	 * {@value}
	 */
	private static final String BPOOL = "bounded" + THREAD_POOL_NAME_TEMPLATE;

	/**
	 * Utility class to provide IDs for pools and threads that are alive.
	 */
	private static final RollingIdGenerator idGenerator = new RollingIdGenerator();

	/**
	 * The default thread factory.
	 */
	private static class CustomThreadFactory implements ThreadFactory {

		/**
		 * {@value}
		 */
		private static final String ERROR_BAD_TEMPLATE = "Thread tmeplate cannot be null or empty.";

		/**
		 * {@value}
		 */
		private static final String ERROR_BAD_POOL_ID = "Pool ID cannot be null or empty";

		private final ThreadGroup group;
		private final String nameTemplate;
		private final String poolId;

		private final RollingIdGenerator rig = new RollingIdGenerator();


		/**
		 * Provides a {@link String#format(String, Object...)} template with one
		 * parameter that assigns the thread ID to the thread name. Good for
		 * logging purposes.
		 * 
		 * @param threadNameTemplate
		 */
		CustomThreadFactory(final String threadNameTemplate, final String poolId) {

			ValidationUtils.checkNullOrEmpty(threadNameTemplate, ERROR_BAD_TEMPLATE);
			ValidationUtils.checkNullOrEmpty(poolId, ERROR_BAD_POOL_ID);

			this.nameTemplate = threadNameTemplate;
			this.poolId = poolId;

			SecurityManager secMgr = System.getSecurityManager();
			this.group =
					(secMgr != null) ? secMgr.getThreadGroup() : Thread.currentThread()
							.getThreadGroup();
		}


		@Override
		public Thread newThread(final Runnable runnable) {

			long threadId = this.rig.getAndIncrement();
			String msg = String.format(this.nameTemplate, this.poolId, threadId);

			Thread newThread = new Thread(this.group, runnable, msg, 0);

			if (newThread.isDaemon()) {
				newThread.setDaemon(false);
			}

			if (newThread.getPriority() != Thread.NORM_PRIORITY) {
				newThread.setPriority(Thread.NORM_PRIORITY);
			}

			return newThread;
		}
	}


	public ExecutorServiceFactory() {

		// prevent instancing.
	}


	/**
	 * Creates a threadpool with the given parameter values.
	 * 
	 * @param aQueueSize
	 *            The size of the work queue to create.
	 * @param aCorNumberOfThreads
	 *            The number of core threads to create when work arrives. The
	 *            queue does not create more until the queue is full.
	 * @param aMaxNumberOfThreads
	 *            The maximum number of threads to create when the queue fills
	 *            up.
	 * @param threadTimeToLive
	 *            The maximum amount of time threads wait for work before
	 *            expiring.
	 * @param timeUnit
	 *            The unit of time to apply to the time to live.
	 * @return An instance of a ExecutorService, does not return null.
	 */
	public static ExecutorService createExecutorService(final int aQueueSize,
			final int aCorNumberOfThreads, final int aMaxNumberOfThreads,
			final long threadTimeToLive, final TimeUnit timeUnit) {

		LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(aQueueSize);

		// Use the queue has as the pool ID.
		String poolId = String.valueOf(idGenerator.getAndIncrement());

		ThreadFactory tf = new CustomThreadFactory(BPOOL, poolId);

		ThreadPoolExecutor threadPool =
				new ThreadPoolExecutor(aCorNumberOfThreads, aMaxNumberOfThreads, threadTimeToLive,
						TimeUnit.SECONDS, queue, tf);

		// Allow the pool to teardown all threads, so JVM can be shutdown
		// without having to depend on a shutdown routine initiating a thread
		// teardown when all the threads are waiting.
		threadPool.allowCoreThreadTimeOut(true);

		// Setup what happens when the work queue is full.
		CallerRunsPolicy saturationHandler = new CallerRunsPolicy();
		threadPool.setRejectedExecutionHandler(saturationHandler);

		return threadPool;
	}


	/**
	 * Creates an executor service using the default algorithm for determining
	 * the number of threads to allocate to the thread pool and using the queue
	 * size and time to live given.
	 * 
	 * @param aQueueSize
	 *            The size of the work queue to create.
	 * @param aThreadTimeToLive
	 *            The maximum amount of time threads wait for work before
	 *            expiring.
	 * @param aTimeUnit
	 *            The unit of time to apply to the time to live.
	 * @return An instance of a ExecutorService, does not return null.
	 */
	public static ExecutorService createExecutorService(final int aQueueSize,
			final long aThreadTimeToLive, final TimeUnit aTimeUnit) {

		int numCores = Runtime.getRuntime().availableProcessors();
		// Just incase the routine returns zero or less, shouldn't happen, but
		// you never know. Small price to pay for a little insurance.
		numCores = (1 > numCores) ? 1 : numCores;
		int initThreads = (DEFAULT_INIT_THREADS < numCores) ? DEFAULT_INIT_THREADS : numCores;
		int numMaxThreads =
				(DEFAULT_INIT_THREADS < numCores) ? numCores + 1 : DEFAULT_INIT_THREADS + 1;

		return createExecutorService(aQueueSize, initThreads, numMaxThreads, aThreadTimeToLive,
				aTimeUnit);
	}


	/**
	 * Creates an executor service using the default algorithm for determining
	 * the number of threads to allocate to the thread pool and using the given
	 * queue size with the default time to live for waiting worker threads.
	 * 
	 * @param aQueueSize
	 *            The size of the work queue to create.
	 * 
	 * @return An instance of a ExecutorService, does not return null.
	 */
	public static ExecutorService createExecutorService(final int aQueueSize) {

		return createExecutorService(aQueueSize, DEFAULT_KEEP_ALIVE_TIME, TimeUnit.SECONDS);
	}


	/**
	 * Creates the threadpool to use for managing threading operations using
	 * default values. The defaults retrieve the number of processors from the
	 * machine and sets the initial and max number of threads according to the
	 * following:
	 * <p/>
	 * <ol>
	 * <li>Set initial number of threads to {@value #DEFAULT_INIT_THREADS} or
	 * the number of processors, which ever is <strong>less</strong></li>
	 * <li>Sets the maximum number of threads to the number of processors + 1</li>
	 * </ol>
	 * 
	 */
	public static ExecutorService createExecutorService() {

		return createExecutorService(DEFAULT_QUEUE_SIZE, DEFAULT_KEEP_ALIVE_TIME, TimeUnit.SECONDS);
	}


	/**
	 * Generates and unbounded pool of threads.
	 * 
	 * @return Instance of ExecutorServices, never returns null.
	 */
	public static ExecutorService createCachedExecutorService() {

		String poolId = String.valueOf(idGenerator.getAndIncrement());
		ThreadFactory tf = new CustomThreadFactory(UBPOOL, poolId);
		return Executors.newCachedThreadPool(tf);
	}

}
