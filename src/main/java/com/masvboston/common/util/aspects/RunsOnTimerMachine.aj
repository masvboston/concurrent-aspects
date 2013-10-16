package com.masvboston.common.util.aspects;

import com.masvboston.common.util.ControllerFactory;
import com.masvboston.common.util.RunsOnTimerController;
import com.masvboston.common.util.annotations.RunsOnTimer;

public aspect RunsOnTimerMachine {

	private static final String ERROR_MSG_RUN_ONCE_ON_FUNCTION =
	        "Error, marking methods with return values with @RunsOnTimer annotation is not supported";

	private static final String ERROR_MSG_RUN_ONCE_WITH_PARAMS =
	        "Error, marking methods with parameters with @RunsOnTimer annotation is not supported";


	/**
	 * Execution of static procedures (not function) marked with
	 * {@link RunsOnTimer} annotation. Any method name, zero parameters on any
	 * class. Capture the target of the method execution. Make sure the
	 * joinpoint is not within this aspect.
	 */
	pointcut staticMethodsMarkedToRunOnTimer(RunsOnTimer a) :
	  execution(@RunsOnTimer static void *.*())
	  && @annotation(a)
	  && !within(RunsOnTimerMachine);


	/**
	 * Execution of procedures (not function) marked with {@link RunsOnTimer}
	 * annotation. Any method name, zero parameters on any class. Capture the
	 * target of the method execution. Make sure the joinpoint is not within
	 * this aspect.
	 */
	pointcut methodsMarkedToRunOnTimer(Object o, RunsOnTimer a) : 
      execution(@RunsOnTimer !static void *())
	  && target(o)
	  && @annotation(a)
	  && !within(RunsOnTimerMachine);


	/**
	 * Identifies methods marked with {@link RunsOnTimer} that return a value.
	 */
	pointcut methodsMarkedRunOnTimerWithReturn() : execution(@RunsOnTimer (!void) *(..));


	/**
	 * Identifies methods marked {@link RunsOnTimer} that return a value.
	 */
	pointcut staticMethodsMarkedRunOnTimerWithReturn() : execution(@RunsOnTimer static (!void) *(..));


	/**
	 * Identifies methods marked {@link RunsOnTimer} that have parameters
	 */
	pointcut methodsMarkedRunOnTimerWithParams() : execution(@RunsOnTimer * *(*, ..));


	/**
	 * Identifies methods marked {@link RunsOnTimer} that have parameters.
	 */
	pointcut staticMethodsMarkedRunOnTimerWithParams() : execution(@RunsOnTimer static * *(*, ..));

	/**
	 * Advice to identify methods marked with the {@link RunsOnTimer} annotation
	 * that should not have the annotation.
	 */
	declare error: methodsMarkedRunOnTimerWithReturn() && !within(RunsOnTimerMachine): 
		"@RunsOnTimer should only be applied to methods that do not return a value";

	/**
	 * Advice to identify methods marked with the {@link RunsOnTimer} annotation
	 * that should not have the annotation.
	 */
	declare error: staticMethodsMarkedRunOnTimerWithReturn() && !within(RunsOnTimerMachine): 
		"@RunsOnTimer should only be applied to methods that do not return a value";

	/**
	 * Advice to identify methods marked with the {@link RunsOnTimer} annotation
	 * that should not have the annotation.
	 */
	declare error: methodsMarkedRunOnTimerWithParams() && !within(RunsOnTimerMachine): 
		"@RunsOnTimer should only be applied to methods that do not have parameters";

	/**
	 * Advice to identify methods marked with the {@link RunsOnTimer} annotation
	 * that should not have the annotation.
	 */
	declare error: staticMethodsMarkedRunOnTimerWithParams() && !within(RunsOnTimerMachine): 
		"@RunsOnTimer should only be applied to methods that do not have parameters";


	/**
	 * Intercepts calls to methods marked with the {@link RunsOnTimer}
	 * annotation and executes them only once.
	 * 
	 * @param obj
	 *            The object owning the marked up method.
	 */
	void around(final Object obj, final RunsOnTimer anno) : methodsMarkedToRunOnTimer(obj, anno){

		RunsOnTimerController rc = ControllerFactory.createRunOnTimerController();

		final Runnable runner = new Runnable() {

			@Override
			public void run() {
				proceed(obj, anno);

			}

		};

		rc.add(obj, thisJoinPointStaticPart, anno.delay(), anno.period(), anno.timeUnit(), runner);
	}


	/**
	 * Catches usage of {@link RunsOnTimer} annotation on functions and throws
	 * an exception to prevent developers from getting undesirable behavior.
	 */
	before() : methodsMarkedRunOnTimerWithReturn() || staticMethodsMarkedRunOnTimerWithReturn() {
		throw new IllegalStateException(ERROR_MSG_RUN_ONCE_ON_FUNCTION);
	}


	/**
	 * Catches usage of {@link RunsOnTimer} annotation on methods with
	 * parameters and throws an exception to enforce proper usage.
	 */
	before() : methodsMarkedRunOnTimerWithParams() || staticMethodsMarkedRunOnTimerWithParams() {
		throw new IllegalStateException(ERROR_MSG_RUN_ONCE_WITH_PARAMS);
	}


	/**
	 * Intercepts calls to static methods marked with the {@link RunsOnTimer}
	 * annotation and executes them only once.
	 * 
	 * @param obj
	 *            The object owning the marked up method.
	 */
	void around(final RunsOnTimer anno) : staticMethodsMarkedToRunOnTimer(anno){

		RunsOnTimerController rc = ControllerFactory.createRunOnTimerController();
		Class<?> clazz = thisJoinPointStaticPart.getSignature().getDeclaringType();

		final Runnable runner = new Runnable() {

			@Override
			public void run() {
				proceed(anno);

			}

		};

		rc.add(clazz, thisJoinPointStaticPart, anno.delay(), anno.period(), anno.timeUnit(), runner);

	}

}
