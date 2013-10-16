package com.masvboston.concurrent.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;


/**
 * Marks a method as requiring all threads that start from wthin it's context to
 * be managed as a group.
 * 
 * @author Mark Miller
 * 
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ThreadRunnableGroup {

	/**
	 * Specifies the amount of time to wait before timing out.
	 * 
	 * @return The amount of time before timing out. Default is 5.
	 */
	int timeOutValue() default 5;


	/**
	 * Specifies the unit of time for the given timeout. Default is minutes.
	 * 
	 * @return The unit of time for the given timeout.
	 */
	TimeUnit timeUnit() default TimeUnit.MINUTES;
}
