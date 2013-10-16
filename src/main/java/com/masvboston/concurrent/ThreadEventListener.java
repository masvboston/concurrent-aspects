package com.masvboston.concurrent;

/**
 * Event interface to use for notification of thread events. It is vitaly
 * important that that any implementation of the methods of this interface
 * do not throw an exception unless it is understood the consequence of
 * doing so on your particular usage of the Thread Machine. So in general,
 * don't throw any exceptions from these methods. Always have catch blocks
 * and handle errors, especially the ones you don't know about.
 * 
 * @author Mark Miller
 * 
 */
public interface ThreadEventListener {

    /**
     * Executes before an event.
     * 
     * @param runnable
     *            The runnable that is about to execute.
     * @return True if the run should continue false to stop execution of
     *         the run.
     */
    boolean beforeThread(Runnable runnable);


    /**
     * Executes when the task is finished running.
     * 
     * @param runnable
     *            The runnable task that just finished.
     */
    void afterThread(Runnable runnable);


    /**
     * Executes when there is an error during execution of the task.
     * 
     * @param runnable
     *            The task throwing the exception.
     * @param error
     *            The exception caught.
     * @return An exception to pass to the default error handler (if any).
     *         Pass null to terminate further propagation of the exception.
     */
    Throwable onException(Runnable runnable, Throwable error);
}