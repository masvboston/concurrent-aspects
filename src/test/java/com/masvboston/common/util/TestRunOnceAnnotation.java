package com.masvboston.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.masvboston.common.util.annotations.RunsOnce;

/**
 * Tests the {@link RunsOnce} annotation for proper operation.
 * 
 * @author Mark Miller, www.masvboston.com
 * 
 */
public class TestRunOnceAnnotation {

	private static class TargetClass {

		static int numberOfInstances = 0;

		int numberOfExecutions = 0;

		static int numberOfStaticExecutions = 0;


		TargetClass() {
			numberOfInstances++;
			System.out.println("Number of instances " + numberOfInstances);
		}


		/**
		 * Uncomment the {@link RunsOnce} annotation here and recompile. There
		 * should be an error reported by the AJDT compiler and then the unit
		 * test should be successful.
		 * 
		 * @return
		 */
		//		@RunsOnce
		boolean bogusFunction() {
			return false;
		}


		/**
		 * Uncomment the {@link RunsOnce} annotation here and recompile. There
		 * should be an error reported by the AJDT compiler and then the unit
		 * test should be successful.
		 * 
		 * @return
		 */
		//		@RunsOnce
		static boolean bogusStaticFunction() {
			return false;
		}

		@Override
		protected void finalize() throws Throwable {
			numberOfInstances--;
			super.finalize();
		}


		@RunsOnce
		void runOnceTestMethod() {
			// Increments the number.
			this.numberOfExecutions++;
			System.out.println("Number of executions " + this.numberOfExecutions);
		}


		@RunsOnce
		static void runOnceStatic() {
			numberOfStaticExecutions++;
			System.out.println("Number of static executions " + numberOfStaticExecutions);
		}
	}


	private static class TargetClass2{

		static int numberOfStaticExecutions = 0;


		@RunsOnce
		static void runOnceStatic() {
			numberOfStaticExecutions++;
			System.out.println("Number of static executions " + numberOfStaticExecutions);
		}
	}


	@Before
	public void terDown() throws InterruptedException {
		/*
		 * Need to run the GC to ensure instances are cleaned up.
		 */
		System.gc();

		/*
		 * Need a little delay to get the GC to work.
		 */
		Thread.sleep(1000);
	}


	/**
	 * No matter how many times the method is called, it should only run once.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testRunOnce() throws InterruptedException {

		TargetClass tc = new TargetClass();

		tc.runOnceTestMethod();
		tc.runOnceTestMethod();
		tc.runOnceTestMethod();

		assertEquals("Number should be 1", 1, tc.numberOfExecutions);

		TargetClass tc2 = new TargetClass();

		tc2.runOnceTestMethod();
		tc2.runOnceTestMethod();
		tc2.runOnceTestMethod();

		assertEquals("Number should be 1", 1, tc2.numberOfExecutions);
		assertEquals("Number of instances should be 2", 2,  TargetClass.numberOfInstances);



		TargetClass.runOnceStatic();
		TargetClass.runOnceStatic();

		assertEquals("Number of static calls should be 1", 1, TargetClass.numberOfStaticExecutions);

		/*
		 * Now null out one of the object and GC to make sure our weak reference
		 * tracking system is working correctly.
		 */
		tc = null;

		// System.runFinalization();
		System.gc();

		// Wait to let the GC catchup.
		Thread.sleep(1000);

		assertEquals("Number of instances should be 1", 1, TargetClass.numberOfInstances);

	}


	/**
	 * This method tests the injection of an exception on code that tries to use
	 * the RunsOnce annotation on functions.
	 */
	@Ignore
	@Test
	public void testRunOnceAnnotationOnFunction() {

		TargetClass tc = new TargetClass();

		try {
			tc.bogusFunction();
			fail("There should be an exception at this point, if not make sure you uncomment "
					+ "the @RunsOnce annotation on method TargetClass.bogusFunction()");
		}
		catch (IllegalStateException e) {
			// All is well.
		}


		try {
			TargetClass.bogusStaticFunction();
			fail("There should be an exception at this point, if not make sure you uncomment "
					+ "the @RunsOnce annotation on method TargetClass.bogusStaticFunction()");
		}
		catch (IllegalStateException e) {
			// All is well.
		}
	}

	@Test
	public void testMultipleClassMethodRuns() {

		TargetClass.runOnceStatic();
		TargetClass2.runOnceStatic();

		TargetClass.runOnceStatic();
		TargetClass2.runOnceStatic();

		TargetClass.runOnceStatic();
		TargetClass2.runOnceStatic();

		assertEquals("Number of static calls should be 1", 1, TargetClass.numberOfStaticExecutions);
		assertEquals("Number of static calls should be 1", 1, TargetClass2.numberOfStaticExecutions);


	}

}
