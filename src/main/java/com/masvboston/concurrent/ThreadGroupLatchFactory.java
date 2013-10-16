package com.masvboston.concurrent;

/**
 * Factory for managing instances of {@link ThreadGroupLatchImpl} instances.
 * 
 * @author Mark Miller
 * 
 */
public class ThreadGroupLatchFactory {

    private ThreadGroupLatchFactory() {

        // Prevent instancing.
    }


    /**
     * Creates an instance of thread group latch according to instance
     * management policies in place fo this factory.
     * 
     * @return An instance of ThreadGroupLatch, never returns null.
     */
    public static final ThreadGroupLatch createThreadGroupLatch() {

        return new ThreadGroupLatchImpl();
    }
}
