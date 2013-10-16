package com.masvboston.common.util.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Annotation that marks a method as one to execute periodically on a timer as
 * long as the instance the method is on is still alive. Only void methods with
 * no parameters are allowed to use this annotation.
 * 
 * @author Mark Miller, www.masvboston.com
 * 
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RunsOnTimer {

	/**
	 * The interval value. Works in combination with {@link #timeUnit()}.
	 * 
	 * @return Default value is 1000.
	 */
	long period() default 1000;


	/**
	 * The time to wait before the first delay. Uses time unit specified by:
	 * {@link #timeUnit()}.
	 * 
	 * @return Zero, 0, is the default value.
	 */
	long delay() default 0;


	/**
	 * Set the time unit to use.
	 * 
	 * @return The unit of time for the given interval value.
	 * @see TimeUnit
	 */
	TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

}
