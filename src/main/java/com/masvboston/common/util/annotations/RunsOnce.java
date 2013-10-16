package com.masvboston.common.util.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that indicates the annotated void method should only run once.
 * Apply to methods that you only want to execute once for the lifetime of the
 * target. This can be a class method or a class instance. Any function or
 * procedure with any number of parameters can use this annotation.
 * 
 * @author Mark Miller, www.masvboston.com
 * 
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RunsOnce {

}
