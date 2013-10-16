package com.masvboston.common.util;

import java.util.concurrent.atomic.AtomicLong;


/**
 * Generates an int ID using an {@link AtomicLong}. The get
 * {@link #getAndIncrement()} method will roll over to {@link Long#MIN_VALUE}
 * when the value of {@link Long#MAX_VALUE} has been reached.
 * 
 * @author mmiller
 * 
 */
public class RollingIdGenerator {

	private final AtomicLong at = new AtomicLong();


	@Override
	public int hashCode() {

		return this.at.hashCode();
	}


	/**
	 * Retrieve the current value.
	 * 
	 * @return The current value.
	 */
	public final long get() {

		return this.at.get();
	}


	/**
	 * Retreive the current value as a short.
	 * 
	 * @return The valuea as a Short.
	 */
	public short shortValue() {

		return this.at.shortValue();
	}


	@Override
	public boolean equals(final Object obj) {

		return this.at.equals(obj);
	}


	/**
	 * Retrieves the current value and then increments it.
	 * 
	 * @return The current value.
	 */
	public final long getAndIncrement() {

		// Do an if check so we avoid locking until we need to.
		if (Long.MAX_VALUE == this.at.get()) {

			synchronized (this.at) {

				// Check again just incase another thread already changed it.
				if (Long.MAX_VALUE == this.at.get()) {
					this.at.set(Long.MIN_VALUE);
					return Long.MAX_VALUE;
				}
			}
		}

		return this.at.getAndIncrement();

	}


	@Override
	public String toString() {

		return this.at.toString();
	}


	/**
	 * Current value as an integer.
	 * 
	 * @return int value
	 */
	public int intValue() {

		return this.at.intValue();
	}


	/**
	 * Return as a long.
	 * 
	 * @return long value.
	 */
	public long longValue() {

		return this.at.longValue();
	}


	/**
	 * Return as a float.
	 * 
	 * @return float value.
	 */
	public float floatValue() {

		return this.at.floatValue();
	}


	/**
	 * Return as a double.
	 * 
	 * @return double value.
	 */
	public double doubleValue() {

		return this.at.doubleValue();
	}

}
