package com.masvboston.concurrent.aspects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.masvboston.concurrent.AbstractThreadEventListenerDecorator;
import com.masvboston.concurrent.ControllerFactory;
import com.masvboston.concurrent.ThreadEventListener;
import com.masvboston.concurrent.ThreadMachineController;
import com.masvboston.concurrent.annotations.ThreadRunnable;
import com.masvboston.concurrent.annotations.ThreadRunnable.ThreadPoolTypeEnum;
import com.masvboston.concurrent.annotations.ThreadRunnableGroup;
import com.masvboston.concurrent.annotations.ThreadRunnableShutdownCheck;
import com.masvboston.concurrent.error.ThreadShutdownException;

/**
 * Tests the various behaviors of the thread machinery
 * 
 * @author Mark Miller
 * 
 */
public class TestThreadMachine {

	/**
	 * Simple logger to use for telemetry
	 */
	private static Logger logger;

	/**
	 * A counter for test references.
	 */
	AtomicInteger int1;

	/**
	 * A second counter for test comparisons between two threads.
	 */
	AtomicInteger int2;

	ThreadMachineController tmc = ControllerFactory.createThreadMachineController();


	@Before
	public void before() {

		Handler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);

		logger = Logger.getAnonymousLogger();
		logger.addHandler(consoleHandler);
		logger.setLevel(Level.ALL);
		logger.info("Logging for unit test setup complete");

		this.int1 = new AtomicInteger();
		this.int2 = new AtomicInteger();

		if (this.tmc.isShutdown()) {
			this.tmc.reset();
		}
	}


	/**
	 * Uncommenting the {@link ThreadRunnable} annotation should result in
	 * compiler warning when built.
	 * 
	 * @return
	 */
	// @ThreadRunnable
	public boolean notThreadableMethod() {

		return false;
	}


	/**
	 * Uncommenting the {@link ThreadRunnable} annotation should result in
	 * compiler warning when built. Since methods cannot be declared to throw a
	 * checked exception.
	 * 
	 * @return
	 */
	@ThreadRunnable
	public void methodWithThrows(final CountDownLatch latch) throws Exception {

		Thread.sleep(1000);
		latch.countDown();
		throw new Exception("Junk exception");
	}


	/**
	 * Uncommenting the {@link ThreadRunnable} annotation and
	 * {@link ThreadRunnableGroup} should result in compiler warning when built
	 * since they cannot be used together.
	 * 
	 * @return
	 */
	// @ThreadRunnable
	// @ThreadRunnableGroup
	public void cannotCombineThreadRunnableAndGroup() {

		// do nothing.
	}


	/**
	 * Executes a loop in a thread. Since the loop logic is within a method
	 * marked as ThreadRunnable the method invocations get wrapped in code that
	 * performs shutdown checks. Has a delay in it which makes the loop take 10
	 * seconds to complete.
	 * 
	 * @param counter
	 */
	@ThreadRunnable
	public void threadTest100LoopsIn10Seconds(final AtomicInteger counter) {

		for (int i = 0; i < 100; i++) {
			logger.fine(Thread.currentThread().getName() + " >>> " + counter.getAndIncrement());

			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
	}


	/**
	 * The code within this method should not have any shutdown checks injected
	 * before them.
	 * 
	 * @param counter
	 * @throws InterruptedException
	 */
	@ThreadRunnable(embedShutdownChecks = false)
	public void disableShutdownChecksThread(final CountDownLatch latch) throws InterruptedException {

		for (int i = 0; i < 100; i++) {
			logger.fine(Thread.currentThread().getName() + "-" + i);

			latch.countDown();

			Thread.sleep(100);
		}
	}


	/**
	 * The code within this method should have shutdown checks injected before
	 * them.
	 * 
	 * @param counter
	 * @throws InterruptedException
	 */
	@ThreadRunnable(embedShutdownChecks = true)
	public void enabledShutdownChecksThread(final CountDownLatch latch) throws InterruptedException {

		for (int i = 0; i < 100; i++) {
			logger.fine(Thread.currentThread().getName() + "-" + i);

			latch.countDown();

			Thread.sleep(100);
		}
	}


	/**
	 * Executes two threads that belong to the same group. This means the caller
	 * of this method waits for both threads to complete prior to returning.
	 * Since the operations are in separate threads, the response should not be
	 * the same as if both threads executed serially.
	 * 
	 * @param i
	 *            counter for the first thread
	 * @param i2
	 *            counter for the second thread.
	 */
	@ThreadRunnableGroup
	public void threadGroup2Threads(final AtomicInteger i, final AtomicInteger i2) {

		threadTest100LoopsIn10Seconds(i);
		threadTest100LoopsIn10Seconds(i2);
	}


	@Test
	public void testSimpleThreadRun() throws InterruptedException {

		threadTest100LoopsIn10Seconds(this.int1);
		threadTest100LoopsIn10Seconds(this.int2);

		Thread.sleep(11000);

		assertEquals(100, this.int1.get());
		assertEquals(100, this.int2.get());
	}


	@Test
	public void testThreadShutdown() throws InterruptedException {

		threadTest100LoopsIn10Seconds(this.int1);
		threadTest100LoopsIn10Seconds(this.int2);

		Thread.sleep(2000);
		this.tmc.shutdown(3, TimeUnit.SECONDS);

		int num1 = this.int1.get();
		int num2 = this.int2.get();

		logger.info("Number of threads 1: " + num1);
		logger.info("Number of threads 2: " + num2);

		Thread.sleep(1000);

		logger.info("Number of threads 1: " + num1);
		logger.info("Number of threads 2: " + num2);

		assertEquals(num1, this.int1.get());
		assertEquals(num2, this.int2.get());

		this.tmc.reset();
	}


	@Test
	public void testThreadRunnableGroupBasic() {

		// Since we're executing items in a group, the system will block on this
		// call until the threads finish.
		threadGroup2Threads(this.int1, this.int2);
		assertEquals(100, this.int1.get());
	}


	/**
	 * In order to test the ability to shutdown a thread group, we must execute
	 * the thread-group from a thread.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testSimpleThreadGroupShutdown() {

		startShutdownThread(2000);

		// Call the group, the execution will be interrupted by our runner
		// requesting a shutdown, and an exception occurs which the test
		// captures.
		try {
			threadGroup2Threads(this.int1, this.int2);
		}
		catch (ThreadShutdownException e) {
			if (this.tmc.isShutdown()) {
				this.tmc.reset();
			}
			else {
				Assert.fail();
			}
		}
	}


	/**
	 * Utility method. Starts an independent thread that signals a shutdown
	 * after given milliseconds.
	 */
	private void startShutdownThread(final long wait) {

		// Create the runner that will execute in another thread to execute a
		// shutdown 2 seconds into the loop.
		Runnable runner = new Runnable() {

			@Override
			public void run() {

				try {
					// Wait one second and then call a shutdown.
					Thread.sleep(wait);
					TestThreadMachine.this.tmc.shutdown(1, TimeUnit.SECONDS);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};

		Thread thread = new Thread(runner);
		thread.start();
	}


	/**
	 * Test the ability to execute a method that declares a checked exception
	 * inside a thread.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMethodWithThrows() throws Exception {

		CountDownLatch latch = new CountDownLatch(1);
		methodWithThrows(latch);
		boolean result = latch.await(2, TimeUnit.SECONDS);
		assertTrue(result);
	}


	/**
	 * Tests the Thread machine shutdown behavior. When shutdown checks are
	 * enabled the thread throws a {@link ThreadShutdownException} when it
	 * detects that a shutdown requests has been made. When shutdown checks are
	 * disabled for a thread, no shutdown checks are injected into the code, but
	 * any sleep or wait operations will be interrupted. Any thread without
	 * sleep / wait will proceed until they finish or they reach the limit
	 * imposed by the {@link ExecutorService#shutdownNow()} method called by
	 * {@link ThreadMachineController};
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testDisableShutdownChecks() throws InterruptedException {

		final boolean[] shutdownState = { false, false };

		// Create an event handler that can intercept exception events and test
		// them.
		class ThreadEventHandler extends AbstractThreadEventListenerDecorator {

			@Override
			public Throwable onException(final Runnable runnable, final Throwable error) {

				shutdownState[0] = false;
				shutdownState[1] = false;

				logger.info("Exception event detected:  " + error.getMessage());

				if (error instanceof ThreadShutdownException) {
					logger.info("Thread shutdown detected");
					shutdownState[0] = true;
				}

				if (error instanceof InterruptedException) {
					logger.info("Thread interrupted");
					shutdownState[1] = true;
				}

				return super.onException(runnable, error);
			}


			public ThreadEventHandler(final ThreadEventListener listener) {

				super(listener);
			}
		}

		ThreadEventListener handler = this.tmc.getThreadEventListener();
		handler = new ThreadEventHandler(handler);
		this.tmc.setThreadEventListener(handler);

		// Try the one that has shutdown checks inserted into the routine.
		CountDownLatch latch = new CountDownLatch(200);
		// Call twice to run 2 threads.
		enabledShutdownChecksThread(latch);
		enabledShutdownChecksThread(latch);
		startShutdownThread(1000);
		boolean result = latch.await(5, TimeUnit.SECONDS);
		assertFalse("Threads should not have completed", result);
		assertFalse("ThreadShutdown exception should have been caught not an interruption",
				shutdownState[1]);
		assertTrue("ThreadShutdown exception should have been caught", shutdownState[0]);

		this.tmc.reset();
		// Try the one that has shutdown check inserts turned off.
		latch = new CountDownLatch(200);
		// Call twice to run 2 threads.
		disableShutdownChecksThread(latch);
		disableShutdownChecksThread(latch);
		startShutdownThread(2000);
		result = latch.await(5, TimeUnit.SECONDS);
		assertFalse(result);
		assertFalse(shutdownState[0]);
		assertTrue(shutdownState[1]);

	}


	/**
	 * Tests the ThreadRunnableShutdownCheck annotations ability to signal a
	 * target to have an explicit shutdown check applied.
	 * 
	 * @throws InterruptedException
	 */
	@Test(expected = ThreadShutdownException.class)
	public void testExplicitShutdownChecksBeforeExec() throws InterruptedException {

		final CountDownLatch latch = new CountDownLatch(50);
		final AtomicInteger numberOfLatchCalls = new AtomicInteger();
		final AtomicInteger numberOfMethodCalls = new AtomicInteger();
		final AtomicInteger numberOfNestedCalls = new AtomicInteger();

		/**
		 * This class tests the explicit shutdown behavior. While in a method
		 * that is configured to not inject shutdown checks, an explicit
		 * shutdown check still functions and will throw an exception when
		 * encountered. The method calls within the method marked with
		 * ThreadRunnable that are not annotated to explicitly check for
		 * shutdown should not invoke an exception.
		 * <p/>
		 * The way this should work is as follows:
		 * 
		 * <ol>
		 * <li>Call the threaded method, which will decrement the latch with
		 * each loop</li>
		 * <li>Once the loop reaches 25 a shutdown request is invoked by the
		 * method configured with an explicit shutdown check</li>
		 * <li>The next round in the loop will trigger an exception at the call
		 * of the explicit shutdown check method</li>
		 * </ol>
		 * 
		 * If implicit shutdown checks are active for the ThreadRunnable then an
		 * exception would occur at any method call prior to the method marked
		 * with the explicit check.
		 * 
		 * 
		 */
		class Target {

			@ThreadRunnable(embedShutdownChecks = false)
			void runInThread() {

				for (int i = 0; i < 100; i++) {
					latch.countDown();
					numberOfLatchCalls.incrementAndGet();
					nestedMethodCall();
					// Once shutdown is requested, loop will not get this far on
					// the next round.
					numberOfMethodCalls.incrementAndGet();
				}
			}


			/**
			 * Using a nested method call to validate that any explicit shutdown
			 * annotation in the call flow of a ThreadRunnable and not just in
			 * the immediate call responds to a shutdown check response.
			 */
			void nestedMethodCall() {

				numberOfNestedCalls.incrementAndGet();
				methodWithExplicitCheck();
			}


			@ThreadRunnableShutdownCheck
			void methodWithExplicitCheck() {

				logger.fine("counter = " + latch.getCount());
				if (25 == latch.getCount()) {
					TestThreadMachine.this.tmc.shutdown(2, TimeUnit.SECONDS);
				}
			}

		}

		Target target = new Target();
		target.methodWithExplicitCheck();
		assertEquals(50, latch.getCount());

		target.runInThread();
		boolean result = latch.await(5, TimeUnit.SECONDS);
		assertFalse(result);
		assertEquals(24, latch.getCount());

		logger.fine("Method calls = " + numberOfMethodCalls.get() + " Latch calls = "
				+ numberOfLatchCalls.get());
		assertEquals(numberOfLatchCalls.get(), numberOfNestedCalls.get());
		assertTrue(numberOfMethodCalls.get() < numberOfLatchCalls.get());

		target.methodWithExplicitCheck();

	}


	/**
	 * Tests the ThreadRunnableShutdownCheck annotations ability to signal a
	 * target to have an explicit shutdown check applied and execute after the
	 * method annotated executes.
	 * 
	 * @throws InterruptedException
	 */
	@Test(expected = ThreadShutdownException.class)
	public void testExplicitShutdownChecksAfterExec() throws InterruptedException {

		final CountDownLatch latch = new CountDownLatch(50);
		final AtomicInteger numberOfLatchCalls = new AtomicInteger();
		final AtomicInteger numberOfMethodCalls = new AtomicInteger();
		final AtomicInteger numberOfNestedCalls = new AtomicInteger();

		/**
		 * This class tests the explicit shutdown behavior. While in a method
		 * that is configured to not inject shutdown checks, an explicit
		 * shutdown check still functions and will throw an exception when
		 * encountered. The method calls within the method marked with
		 * ThreadRunnable that are not annotated to explicitly check for
		 * shutdown should not invoke an exception.
		 * <p/>
		 * The way this should work is as follows:
		 * 
		 * <ol>
		 * <li>Call the threaded method, which will decrement the latch with
		 * each loop</li>
		 * <li>Once the loop reaches 25 a shutdown request is invoked by the
		 * method configured with an explicit shutdown check</li>
		 * <li>Since the shutdown check occurs after the method, the thread will
		 * not execute another loop, but instead encounter an exception.</li>
		 * </ol>
		 * 
		 * If implicit shutdown checks are active for the ThreadRunnable then an
		 * exception would occur at any method call prior to the method marked
		 * with the explicit check.
		 * 
		 * 
		 */
		class Target {

			@ThreadRunnable(embedShutdownChecks = false)
			void runInThread() {

				for (int i = 0; i < 100; i++) {
					latch.countDown();
					numberOfLatchCalls.incrementAndGet();
					nestedMethodCall();
					// Once shutdown is requested, loop will not get this far on
					// the next round.
					numberOfMethodCalls.incrementAndGet();
				}
			}


			/**
			 * Using a nested method call to validate that any explicit shutdown
			 * annotation in the call flow of a ThreadRunnable and not just in
			 * the immediate call responds to a shutdown check response.
			 */
			void nestedMethodCall() {

				numberOfNestedCalls.incrementAndGet();
				methodWithExplicitCheck();
			}


			@ThreadRunnableShutdownCheck(afterExecution = true)
			void methodWithExplicitCheck() {

				logger.fine("counter = " + latch.getCount());
				if (25 == latch.getCount()) {
					TestThreadMachine.this.tmc.shutdown(2, TimeUnit.SECONDS);
				}
			}

		}

		Target target = new Target();
		target.methodWithExplicitCheck();
		assertEquals(50, latch.getCount());

		target.runInThread();
		boolean result = latch.await(5, TimeUnit.SECONDS);
		assertFalse(result);
		assertEquals(25, latch.getCount());

		logger.fine("Method calls = " + numberOfMethodCalls.get() + " Latch calls = "
				+ numberOfLatchCalls.get());
		assertEquals(numberOfLatchCalls.get(), numberOfNestedCalls.get());
		assertTrue(numberOfMethodCalls.get() < numberOfLatchCalls.get());

		target.methodWithExplicitCheck();

	}


	/**
	 * This test checks to ensure the default pooling logic works. When a thread
	 * is set to be poolable it should run in a default pool with a default pool
	 * name.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testDefaultThreadPoolingTypeLogic() throws InterruptedException {

		final CountDownLatch c1 = new CountDownLatch(100);

		class Target {

			@ThreadRunnable(poolable = true)
			void runInDefaultPool() throws InterruptedException {

				countTo100(c1);

			}


			private void countTo100(final CountDownLatch cl) {

				for (int i = 0; i < 100; i++) {
					logger.fine(Thread.currentThread().getName() + "-" + i);
					cl.countDown();
				}
			}
		}

		Target t = new Target();

		t.runInDefaultPool();

		assertTrue(c1.await(5, TimeUnit.SECONDS));
	}


	/**
	 * This test checks to ensure the local pooling logic works.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testClassThreadPoolingTypeLogic() throws InterruptedException {

		final CountDownLatch cl = new CountDownLatch(100);

		class Target {

			@ThreadRunnable(poolable = true, poolType = ThreadPoolTypeEnum.CLASS)
			void runInLocalPool() throws InterruptedException {

				countTo100(cl);

			}


			private void countTo100(final CountDownLatch cl) {

				for (int i = 0; i < 100; i++) {
					logger.fine(Thread.currentThread().getName() + "-" + i);
					cl.countDown();
				}
			}
		}

		Target t = new Target();

		t.runInLocalPool();

		assertTrue(cl.await(5, TimeUnit.SECONDS));

	}


	/**
	 * This test checks to ensure the local pooling logic works.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testInstanceThreadPoolingTypeLogic() throws InterruptedException {

		final CountDownLatch cl = new CountDownLatch(100);

		class Target {

			@ThreadRunnable(poolable = true, poolType = ThreadPoolTypeEnum.INSTANCE)
			void runInInstancePool() throws InterruptedException {

				countTo100(cl);

			}


			private void countTo100(final CountDownLatch cl) {

				for (int i = 0; i < 100; i++) {
					logger.fine(Thread.currentThread().getName() + "-" + i);
					cl.countDown();
				}
			}
		}

		Target t = new Target();

		t.runInInstancePool();

		assertTrue(cl.await(5, TimeUnit.SECONDS));

	}


	/**
	 * This test checks to ensure the master pooling logic works.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testMasterThreadPoolingTypeLogic() throws InterruptedException {

		final CountDownLatch cl = new CountDownLatch(100);

		class Target {

			@ThreadRunnable(poolable = true, poolType = ThreadPoolTypeEnum.MASTER)
			void runInMasterPool() throws InterruptedException {

				countTo100(cl);

			}


			private void countTo100(final CountDownLatch cl) {

				for (int i = 0; i < 100; i++) {
					logger.fine(Thread.currentThread().getName() + "-" + i);
					cl.countDown();
				}
			}
		}

		Target t = new Target();

		t.runInMasterPool();

		assertTrue(cl.await(5, TimeUnit.SECONDS));
	}


	/**
	 * This test checks to ensure the named pooling logic works.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testNamedThreadPoolingTypeLogic() throws InterruptedException {

		final CountDownLatch cl = new CountDownLatch(100);

		class Target {

			@ThreadRunnable(poolable = true, poolType = ThreadPoolTypeEnum.NAMED, threadPoolName = "markspool")
			void runInNamedPool() throws InterruptedException {

				countTo100(cl);

			}


			private void countTo100(final CountDownLatch cl) {

				for (int i = 0; i < 100; i++) {
					logger.fine(Thread.currentThread().getName() + "-" + i);
					cl.countDown();
				}
			}
		}

		Target t = new Target();

		t.runInNamedPool();

		assertTrue(cl.await(5, TimeUnit.SECONDS));
	}


	/**
	 * This test checks to ensure all pooling logic works together on same
	 * class.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testCombinedThreadPoolingTypeLogic() throws InterruptedException {

		final CountDownLatch c1 = new CountDownLatch(100);
		final CountDownLatch c2 = new CountDownLatch(100);
		final CountDownLatch c3 = new CountDownLatch(100);
		final CountDownLatch c4 = new CountDownLatch(100);
		final CountDownLatch c5 = new CountDownLatch(100);

		class Target {

			@ThreadRunnable(poolable = true)
			void runInDefaultPool() throws InterruptedException {

				countTo100(c1);

			}


			@ThreadRunnable(poolable = true, poolType = ThreadPoolTypeEnum.CLASS)
			void runInLocalPool() throws InterruptedException {

				countTo100(c2);

			}


			@ThreadRunnable(poolable = true, poolType = ThreadPoolTypeEnum.MASTER)
			void runInMasterPool() throws InterruptedException {

				countTo100(c3);

			}


			@ThreadRunnable(poolable = true, poolType = ThreadPoolTypeEnum.NAMED, threadPoolName = "markspool")
			void runInNamedPool() throws InterruptedException {

				countTo100(c4);

			}


			@ThreadRunnable(poolable = true, poolType = ThreadPoolTypeEnum.INSTANCE)
			void runInInstancePool() throws InterruptedException {

				countTo100(c5);

			}


			private void countTo100(final CountDownLatch cl) {

				for (int i = 0; i < 100; i++) {
					logger.fine(Thread.currentThread().getName() + "-" + i);
					cl.countDown();
				}
			}
		}

		Target t = new Target();

		t.runInDefaultPool();
		t.runInLocalPool();
		t.runInMasterPool();
		t.runInNamedPool();
		t.runInInstancePool();

		assertTrue(c1.await(5, TimeUnit.SECONDS));
		assertTrue(c2.await(5, TimeUnit.SECONDS));
		assertTrue(c3.await(5, TimeUnit.SECONDS));
		assertTrue(c4.await(5, TimeUnit.SECONDS));
	}

}
