package com.masvboston.concurrent.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.masvboston.concurrent.ThreadMachineController;


/**
 * Marks a method as runnable by a thread. The method must be construction in a
 * manner allowing it to accumulate results after the execution in the thread
 * completes.
 * 
 * This annotation has values that inform the annotation processor as to how to
 * handle threaded execution.
 * 
 * @author Mark Miller
 * 
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ThreadRunnable {

	/**
	 * The type of thread pooled used for a poolable annotated method.
	 * 
	 * @author mmiller
	 * 
	 */
	enum ThreadPoolTypeEnum {
		/**
		 * Use the one master thread pool for invoking this thread activity.
		 * There is only one thread master thread pool for the entire process.
		 */
		MASTER,
		/**
		 * Use a thread pool specifically created for the class. The threaded
		 * methods share one pool created using the class as the name of the
		 * pool. This means all threaded methods on that class use one pool
		 * created only once for the process.
		 */
		CLASS,
		/**
		 * Use a thread pool specifically created for each
		 * <strong>instance</strong> of the class. This means all threaded
		 * methods called on an <string>instance</strong> of a class will share
		 * a pool that is bound to the instance of the class.
		 */
		INSTANCE,
		/**
		 * Use a specifically named thread pool. Keep in mind if an annotation
		 * somewhere else in the code base specifies this name then that named
		 * pool is used, even if you did not specify that name.
		 */
		NAMED
	}


	/**
	 * Determines if the thread pool used is the master thread pool, a local one
	 * created just for the annotated method, or a specifically named pool. The
	 * default value is {@link ThreadPoolTypeEnum#MASTER}.
	 * 
	 * @return Should never be null.
	 * @see ThreadPoolTypeEnum
	 * @see #poolable()
	 * @see #threadPoolName()
	 */
	ThreadPoolTypeEnum poolType() default ThreadPoolTypeEnum.MASTER;


	/**
	 * Specifies whether the thread can operate in a pool. Set to false to
	 * ensure threads are not from a bounded thread pool, true if you want
	 * threads to come from a thread pool. Use {@link #poolType()} to modify
	 * what pool is used. This field is here instead of using an enum to control
	 * enabling of pooling to provide a means of using a central switch to turn
	 * pooling on and off for a group of annotations preset with their specific
	 * thread pool type.
	 * 
	 * @return True if the thread should be in a pool, false if not, default is
	 *         false.
	 * @see #poolType()
	 * @see #threadPoolName()
	 * @see ThreadPoolTypeEnum
	 */
	boolean poolable() default false;


	/**
	 * The specific thread pool the thread will belong to.
	 * 
	 * @return Null if no pool name. It is an error not to specify a thread pool
	 *         name but have the thread pool type set to
	 *         {@link ThreadPoolTypeEnum#NAMED}. This value also cannot be an
	 *         empty string.
	 * 
	 * @see ThreadPoolTypeEnum
	 * @see #poolable()
	 * @see #poolType()
	 */
	String threadPoolName() default "";


	/**
	 * Indicates whether the thread is allowed to be placed into groups
	 * 
	 * @return True if the thread should be in groups, false otherwise. Default
	 *         is true.
	 */
	boolean groupable() default true;


	/**
	 * Instructs the thread machinery to insert checks for thread shutdown in
	 * the annotated code.
	 * 
	 * @return True if the engine should insert checks for shutdown code, false
	 *         if it should not insert any code that checks for the shutdown
	 *         flag.
	 * 
	 * @see ThreadMachineController#shutdown(long,
	 *      java.util.concurrent.TimeUnit)
	 */
	boolean embedShutdownChecks() default true;

}
