package com.masvboston.common.util;

import org.aspectj.lang.JoinPoint;

/**
 * Utility class for managing the run-once execution of methods. This class
 * tracks objects liveness and their corresponding method executions to
 * facilitate single execution of a method. This class is thread safe.
 * 
 * @author Mark Miller, www.masvboston.com
 * 
 */
public class RunOnceController {

	/**
	 * Reference to queue being used to track each method call per object or
	 * class instance.
	 */
	private final InstanceAttributeTracker<Object, JoinPoint.StaticPart, Object> methodTacker;


	/**
	 * Constructor to initialize all data structures.
	 */
	public RunOnceController() {
		this.methodTacker = new InstanceAttributeTracker<Object, JoinPoint.StaticPart, Object>();
	}


	/**
	 * Method to execute the given callback if the object and method have not
	 * executed before. The call back is merely a reference to a runnable of
	 * which the run method will only execute if the given instance and method
	 * have never executed before.
	 * 
	 * @param instance
	 *            The instance the method belongs to.
	 * @param method
	 *            The method identifier of the instance.
	 * @param callBack
	 *            The callback to execute if this has never run before.
	 * @return True if callback executed false otherwise.
	 */
	public boolean executeAndCatalog(final Object instance, final JoinPoint.StaticPart method,
			final Runnable callBack) {

		synchronized (this.methodTacker) {

			boolean result = this.methodTacker.checkAndAdd(instance, method, null);

			if (result) {
				callBack.run();
			}

			return result;
		}

	}
}
