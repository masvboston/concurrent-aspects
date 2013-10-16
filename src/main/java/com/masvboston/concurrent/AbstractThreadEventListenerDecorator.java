package com.masvboston.concurrent;

import com.masvboston.common.util.ValidationUtils;


/**
 * Base decorator that forwards all calls to the wrapped decorator. Override the
 * appropriate method change the behavior.
 * 
 * @author Mark Miller
 * 
 */
public abstract class AbstractThreadEventListenerDecorator implements ThreadEventListener {

    /**
     * {@value}
     */
    private static final String ERROR_TARGET_NULL = "Target listener cannot be null";

    /**
     * Reference to target.
     */
    private final ThreadEventListener target;


    public AbstractThreadEventListenerDecorator(final ThreadEventListener listener) {

        ValidationUtils.checkNull(listener, ERROR_TARGET_NULL);
        this.target = listener;
    }


    @Override
    public boolean beforeThread(final Runnable runnable) {

        return this.target.beforeThread(runnable);
    }


    @Override
    public void afterThread(final Runnable runnable) {

        this.target.afterThread(runnable);
    }


    @Override
    public Throwable onException(final Runnable runnable, final Throwable error) {

        return this.target.onException(runnable, error);
    }

}
