package com.masvboston.concurrent.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * Marks methods <strong>declarations</strong> as targets for shutdown checks
 * prior to execution.
 * 
 * @author Mark Miller
 * 
 */
@Documented
@Target({ METHOD })
@Retention(RUNTIME)
public @interface ThreadRunnableShutdownCheck {

    /**
     * Indicates the shutdown check should occur after execution.
     * 
     * @return True if it should occur after execution, false if it should occur
     *         before, default is false.
     */
    boolean afterExecution() default false;
}
