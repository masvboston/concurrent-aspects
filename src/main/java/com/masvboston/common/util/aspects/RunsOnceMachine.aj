package com.masvboston.common.util.aspects;

import com.masvboston.common.util.RunOnceController;
import com.masvboston.common.util.annotations.RunsOnce;
import com.masvboston.common.util.ControllerFactory;

/**
 * Aspect that controls the execution of methods annotated with {@link RunsOnce}.
 * The annotation is only allowed on procedures not functions. This is due to
 * the fact that although a mechanism can be emplaced that stores the result
 * value, it cannot identify situations where a field used a storage for that
 * value from within the method gets changed from another part of the class or
 * externally. This can lead to confusion where the code appears to be changing
 * what the function would normally return yet the code is returning the older
 * value, the one produced by the first and only complete call of the method.
 * 
 * @author Mark Miller, mmiller@masvboston.com
 * 
 */
public aspect RunsOnceMachine {

	private static final String ERROR_MSG_RUN_ONCE_ON_FUNCTION =
	        "Error, marking methods with return values with @RunsOnce annotation will result "
	                + "in confusing behavior subsequent calls.";


	/**
	 * Execution of static procedures (not function) marked with {@link RunsOnce}
	 * annotation. Any method name, any number of parameters on any class.
	 * Capture the target of the method execution. Make sure the joinpoint is
	 * not within this aspect.
	 */
	pointcut staticMethodsMarkedToRunOnce() :
	  execution(@RunsOnce static void *.*(..))
	  && !within(RunsOnceMachine);


	/**
	 * Execution of procedures (not function) marked with {@link RunsOnce}
	 * annotation. Any method name, any number of parameters on any class.
	 * Capture the target of the method execution. Make sure the joinpoint is
	 * not within this aspect.
	 */
	pointcut methodsMarkedToRunOnce(Object o) : 
      execution(@RunsOnce !static void *(..))
	  && target(o)
	  && !within(RunsOnceMachine);


	/**
	 * Identifies methods marked {@link RunsOnce} that return a value.
	 */
	pointcut methodsMarkedRunOnceWithReturn() : execution(@RunsOnce (!void) *(..));


	/**
	 * Identifies methods marked {@link RunsOnce} that return a value.
	 */
	pointcut staticMethodsMarkedRunOnceWithReturn() : execution(@RunsOnce static (!void) *(..));

	/**
	 * Advice to identify methods marked with the {@link RunsOnce} annotation
	 * that should not have the annotation.
	 */
	declare error: methodsMarkedRunOnceWithReturn() && !within(RunsOnceMachine): 
		"@RunsOnce should only be applied to methods that do not return a value";

	/**
	 * Advice to identify methods marked with the {@link RunsOnce} annotation
	 * that should not have the annotation.
	 */
	declare error: staticMethodsMarkedRunOnceWithReturn() && !within(RunsOnceMachine): 
		"@RunsOnce should only be applied to methods that do not return a value";


	/**
	 * Intercepts calls to methods marked with the {@link RunsOnce} annotation
	 * and executes them only once.
	 * 
	 * @param obj
	 *            The object owning the marked up method.
	 */
	void around(final Object obj) : methodsMarkedToRunOnce(obj){

		RunOnceController rc = ControllerFactory.createRunOnceExectuionController();

		final Runnable runner = new Runnable() {

			@Override
			public void run() {
				proceed(obj);

			}

		};

		rc.executeAndCatalog(obj, thisJoinPointStaticPart, runner);
	}


	/**
	 * Intercepts calls to static methods marked with the {@link RunsOnce}
	 * annotation and executes them only once.
	 * 
	 * @param obj
	 *            The object owning the marked up method.
	 */
	void around() : staticMethodsMarkedToRunOnce(){

		RunOnceController rc = ControllerFactory.createRunOnceExectuionController();
		Class<?> clazz = thisJoinPointStaticPart.getSignature().getDeclaringType();

		final Runnable runner = new Runnable() {

			@Override
			public void run() {
				proceed();

			}

		};

		rc.executeAndCatalog(clazz, thisJoinPointStaticPart, runner);

	}


	/**
	 * Catches usage of {@link RunsOnce} annotation on functions and throws an
	 * exception to prevent developers from getting undesirable behavior.
	 */
	before() : methodsMarkedRunOnceWithReturn() || staticMethodsMarkedRunOnceWithReturn() {
		throw new IllegalStateException(ERROR_MSG_RUN_ONCE_ON_FUNCTION);
	}

}
