package com.masvboston.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

import com.masvboston.common.util.annotations.RunsOnTimer;

public class TestRunOnTimerAnnotation {

	public static abstract class Target {

		abstract void run();


		@RunsOnTimer
		static void staticTest() {
			System.out.println("Made it to static testing");
		}


		/*
		 * Remove comment on annotation to verify that aspect picks up
		 * erroneous use. Should produce a compiler error and trying to run it
		 * will generate an exception.
		 */
		// @RunsOnTimer
		void run(final Object a) {
			fail("Should never have made it this far");
		}


		/*
		 * Remove comment on annotation to verify that aspect picks up
		 * erroneous use. Should produce a compiler error and trying to run it
		 * will generate an exception.
		 */
		// @RunsOnTimer
		boolean function() {
			return true;
		}


		/*
		 * Remove comment on annotation to verify that aspect picks up
		 * erroneous use. Should produce a compiler error and trying to run it
		 * will generate an exception.
		 */
		// @RunsOnTimer
		boolean function(final Object a) {
			return true;
		}


		/*
		 * Remove comment on annotation to verify that aspect picks up
		 * erroneous use. Should produce a compiler error and trying to run it
		 * will generate an exception.
		 */
		// @RunsOnTimer
		static void staticTest(final Object a) {
			fail("Should never have made it this far");
		}

	}


	@Test
	public void testSimpleRun() throws InterruptedException {

		final CountDownLatch latch = new CountDownLatch(12);
		final CountDownLatch latch2 = new CountDownLatch(3);

		Target target = new Target() {

			@Override
			@RunsOnTimer(period = 500, timeUnit = TimeUnit.MILLISECONDS)
			public void run() {
				System.out.println("A Running");
				latch.countDown();
				System.out.println("A Counted down");
			}
		};

		Target target2 = new Target() {

			@Override
			@RunsOnTimer(delay = 5000, period = 5000, timeUnit = TimeUnit.MILLISECONDS)
			public void run() {
				System.out.println("B Running");
				latch2.countDown();
				System.out.println("B Counted down");
			}
		};

		target.run();
		target2.run();

		boolean result = latch.await(20, TimeUnit.SECONDS);
		assertTrue("Timer didn't work", result);

		/*
		 * At this point there should have been enough time for the second latch
		 * to execute at least once, the the count should be 1 less.
		 */
		assertEquals("Second timer fire not expected value", 2, latch2.getCount());

		result = latch2.await(20, TimeUnit.SECONDS);
		assertTrue("Timer didn't work", result);

	}


	@Ignore
	@Test
	public void testBadMethodCalls() {

		final CountDownLatch latch = new CountDownLatch(12);

		Target target = new Target() {

			@Override
			@RunsOnTimer(delay = 5000, period = 5000, timeUnit = TimeUnit.MILLISECONDS)
			public void run() {
				System.out.println("C Running");
				latch.countDown();
				System.out.println("C Counted down");
			}
		};

		Target.staticTest();

		try {
			Target.staticTest("Hello");
			fail();
		}
		catch (IllegalStateException e) {
			// Works great.
		}

		try {
			target.run("Hello");
			fail();
		}
		catch (IllegalStateException e) {
			// Works great.
		}

	}

}
