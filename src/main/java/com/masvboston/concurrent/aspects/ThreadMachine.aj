package com.masvboston.concurrent.aspects;

import java.util.UUID;

import com.masvboston.concurrent.ControllerFactory;
import com.masvboston.concurrent.ThreadMachineController;
import com.masvboston.concurrent.annotations.ThreadRunnable;
import com.masvboston.concurrent.annotations.ThreadRunnableGroup;
import com.masvboston.concurrent.annotations.ThreadRunnableShutdownCheck;
import com.masvboston.concurrent.error.ThreadException;
import com.masvboston.concurrent.error.ThreadShutdownException;

/**
 * Aspect for executing specially annotated methods in threads. Mark methods
 * with the {@link ThreadRunnable} annotation to have the aspect execute them in
 * their own thread every time they are invoked. Methods must have their own
 * mechanisms for managing any output from the execution of the method in a
 * separate thread.
 * <p/>
 * Mark methods with {@link ThreadRunnableGroup} to manage execution of multiple
 * threads as a group. The method marked with {@link ThreadRunnableGroup} will
 * block until all threads in its group finish.
 * </p>
 * <strong>Note:</strong> Shutdown check code is injected before the execution
 * of code marked with the {@link ThreadRunnable} annotation. This occurs for
 * all qualifying joinpoints within the method. Should any code invoke the
 * {@link ThreadMachineController#shutdown(long, java.util.concurrent.TimeUnit)}
 * method, all threads appropriately annotated will terminate their run once the
 * injected shutdown checks see the shutdown notification.
 * <em>You can <strong>disable</strong> shutdown checks by setting the 
 * {@link ThreadRunnable#embedShutdownChecks()}
 * to false.</em>
 * 
 * @author Mark Miller
 * 
 */
public aspect ThreadMachine issingleton(){

	/**
	 * {@value}
	 */
	private static final String ERROR_UNKNOWN_TP_TYPE =
	        "Unknown thread pool type, make sure you update the Thread Machine code for type: ";

	/**
     * {@value}
     */
	private static final String ERROR_SHUTDOWN =
	        "Shutdown request initiated, terminating method execution.";

	/**
	 * Reference to master thread executor
	 */
	private static final String MASTER_EXECUTOR_ID = UUID.randomUUID().toString();


	/**
	 * Must have {@link ThreadRunnable} annotation. Is a method. Returns void.
	 * Takes any number of parameters. Any visibility modifier.
	 * 
	 */
	pointcut threadRunnable() : execution(@ThreadRunnable void *(..));


	/**
	 * Captures all join points within methods marked as {@link ThreadRunnable}.
	 */
	pointcut withinThreadRunnable(ThreadRunnable tr) : withincode(@ThreadRunnable void *(..)) &&
      @withincode(tr);


	/**
	 * Must have {@link ThreadRunnableGroup} annotation. Is a method. Returns
	 * void. Takes any number of parameters. Any visibility modifier.
	 */
	pointcut threadRunnableGroup() : execution(@ThreadRunnableGroup void *(..));


	/**
	 * Identifies methods marked ThreadRunnable that return a value.
	 */
	pointcut threadRunnableWithReturn() : execution(@(ThreadRunnable || ThreadRunnableGroup) 
			(!void) *(..));


	/**
	 * Identifies all method executions that qualify for executing in a thread.
	 * 
	 * @param tr
	 *            A reference to the annotation.
	 */
	pointcut threadRunnableExec(ThreadRunnable tr) : @annotation(tr) &&
      threadRunnable() && !threadRunnableGroup()  && !within(ThreadMachine);


	/**
	 * Identifies all places where an explicit shutdown check should occur
	 * before the method executes.
	 */
	pointcut explicitShutdownCheck(ThreadRunnableShutdownCheck trc) : 
        execution(@ThreadRunnableShutdownCheck * *(..)) &&
        @annotation(trc) && !within(ThreadMachine);


	/**
	 * Identifies all method executions that qualify to have all thread
	 * executions that occur within their thread context to be grouped together.
	 * 
	 * @param trg
	 *            The thread group annotation reference for extracting
	 *            information the thread group specifies, if any.
	 */
	pointcut threadRunnableGroupExec(ThreadRunnableGroup trg) : threadRunnableGroup() && 
      @annotation(trg) && !threadRunnable() && !within(ThreadMachine);


	/**
	 * Identifies places to add shutdown checks.
	 * 
	 * @param tr
	 *            The thread runnable annotation marking candidates for shutdown
	 *            checks. If the value of embedShutdownChecks is true then
	 *            shutdowns will get inserted.
	 */
	pointcut withinThreadRunnableExec(ThreadRunnable tr) : withinThreadRunnable(tr)  && 
      if(tr.embedShutdownChecks()) && !within(ThreadMachine);

	/**
	 * Advice to identify methods marked with the thread control annotation that
	 * are not suitable to be run in a thread.
	 */
	declare error: threadRunnableWithReturn() && !within(ThreadMachine): 
		"@ThreadRunnable should only be applied to methods that do not return a value";

	/**
	 * Advice to identify methods that have both the thread runnable and thread
	 * runnable grouping annotation applied, which is not supported.
	 */
	declare error: threadRunnable() && threadRunnableGroup() && !within(ThreadMachine) :
		"@ThreadRunnableGroup cannot be combined with @ThreadRunnabe on the same method";


	/**
	 * Advice that intercepts calls to methods marked with the
	 * {@link ThreadRunnable} annotation and places the execution in a separate
	 * thread.
	 */
	void around(final ThreadRunnable tr, final Object obj) : threadRunnableExec(tr) && target(obj)
	{

		/*
		 * This will be the default pool name if none is provided by the user.
		 */
		String poolName = tr.threadPoolName();

		/*
		 * If it is poolable then determine what kind of pool it should be.
		 */
		if (tr.poolable()) {

			switch (tr.poolType()) {
				case MASTER:
					/*
					 * This uses one thread pool for the entire process to
					 * execute the method.
					 */
					poolName = MASTER_EXECUTOR_ID;
					break;
				case CLASS:
					/*
					 * This uses the full class name as the key, which means the
					 * local thread pool for all threaded methods in a class
					 * specifying a local thread pool will use the same one.
					 */
					poolName = obj.getClass().getName();
					break;

				case INSTANCE:
					/*
					 * Uses the class name and the hashcode as the key to the
					 * thread pool. The combination ensures all methods will
					 * execute in threads that come from a pool bound to the
					 * instance of the class not just one for the class itself.
					 */
					poolName = obj.getClass().getName() + obj.hashCode();
					break;
				case NAMED:
					/*
					 * do nothing because poolname is already retrieved before
					 * this point.
					 */
					break;
				default:
					/*
					 * This should not happen because all types should be
					 * accounted for, but just incase we have an indicator.
					 */
					throw new ThreadException(ERROR_UNKNOWN_TP_TYPE + tr.poolType().name());
			}
		}

		/*
		 * Create the runnable for the thread.
		 */
		final Runnable runnable = new Runnable() {

			@Override
			public void run() {

				// have the aspect continue the execution of the method.
				proceed(tr, obj);

			}
		};

		/*
		 * Pass to the controller to have the method executed as it sees fit.
		 */
		ControllerFactory.createThreadMachineController().executeInThread(tr.poolable(), poolName,
		        tr.groupable(), runnable);

	}


	/**
	 * Sets up a thread group for the running thread context for all methods
	 * annotated with the {@link ThreadRunnableGroup} annotation.
	 */
	before(final ThreadRunnableGroup trg) : threadRunnableGroupExec(trg) {

		// The reference to the annotation is not used here.

		ControllerFactory.createThreadMachineController().createThreadGroup();
	}


	/**
	 * Advises all joinpoints in the method annotated with ThreadRunnable with a
	 * check to see if a shutdown was requested, and if so an exception is
	 * thrown to disable execution.
	 */
	before(final ThreadRunnable tr) : withinThreadRunnableExec(tr){

		checkForShutdown();

	}


	/**
	 * Advises all joinpoints where a check for a shutdown appears before
	 * execution of the method.
	 * 
	 * @see ThreadRunnableShutdownCheck#shutdownCheck()
	 * 
	 * @param trc
	 *            The current annotation being captured.
	 */
	before(final ThreadRunnableShutdownCheck trc) : explicitShutdownCheck(trc) && 
      if(!trc.afterExecution()){

		checkForShutdown();
	}


	/**
	 * Utility method that checks the shutdown flag and throws an exception if
	 * the system requests a shutdown.
	 */
	private void checkForShutdown() {

		boolean shutdownRequested = ControllerFactory.createThreadMachineController().isShutdown();

		if (shutdownRequested) {
			throw new ThreadShutdownException(ERROR_SHUTDOWN);
		}
	}


	/**
	 * Captures all methods that are marked as having all subsequent thread
	 * starts to be in a thread group. All calls to these methods will not
	 * return until all child threads invoked on their call flow complete.
	 * 
	 * @see ThreadRunnableGroup
	 */
	after(final ThreadRunnableGroup trg) : threadRunnableGroupExec(trg)
	{

		ControllerFactory.createThreadMachineController().awaitCurrentThreadGroup(
		        trg.timeOutValue(), trg.timeUnit());
	}


	/**
	 * Advises all joinpoints where a check for a shutdown appears after
	 * execution of the method.
	 * 
	 * @see ThreadRunnableShutdownCheck#shutdownCheck()
	 * 
	 * @param trc
	 *            The current annotation being captured.
	 */
	after(final ThreadRunnableShutdownCheck trc) : explicitShutdownCheck(trc) && 
      if(true == trc.afterExecution()){

		checkForShutdown();
	}

}
