package com.masvboston.common.util.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Annotation that marks a method as having a time period in which it must
 * complete, a timeout. If the execution time exceeds the timeout period a
 * {@link TimeoutException} is thrown.
 * 
 * @author Mark Miller, www.masvboston.com
 * 
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeOut {

	/**
	 * Specifies the amount of time to wait for the method to complete.
	 * 
	 * @return Default is 1000.
	 */
	long timeOut() default 1000;


	/**
	 * Specifies the time unit for the {@link #timeOut()} value.
	 * 
	 * @return Some unit of {@link TimeUnit} Default is
	 *         {@link TimeUnit#MILLISECONDS}.
	 */
	TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

}
