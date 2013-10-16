package com.masvboston.common.util;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Collection that checks periodically for stale entries and removes them. This
 * is behavior in addition to checking for dead entries with every request. This
 * class will relieve itself of stale entries on its own without any code
 * accessing it. This is in contrast to {@link TimeToLiveCollection} which will
 * only check for stale entries upon invoking it's methods.
 * 
 * This class is thread safe.
 * 
 * @author Mark Miller, www.masvboston.com
 * 
 * @param <T>
 *            The Type the collection will store.
 */
public class AutoTimeToLiveCollection<T> extends TimeToLiveCollection<T> {

	/**
	 * {@value}
	 */
	private static final String COLLECTION_LABEL = "AUTO TTL COLLECTION";


	/**
	 * Create a collection of objects that are removed from the collection if
	 * they have been in the collection too long.
	 * 
	 * @param targetCollection
	 *            An existing collection of items you wish to transfer.
	 * @param timeToLive
	 *            The amount of time each entry has to live.
	 * @param timeUnit
	 *            The unit of time for the time to live value.
	 */
	public AutoTimeToLiveCollection(final Collection<T> targetCollection, final long timeToLive,
			final TimeUnit timeUnit) {

		super(targetCollection, timeToLive, timeUnit);
		init(timeToLive, timeUnit);
	}


	/**
	 * Initializes the timed routine.
	 * 
	 * @param timeToLive
	 *            The amount of time each entry has to live.
	 * @param timeUnit
	 *            The unit of time for the time to live value.
	 */
	private void init(final long timeToLive, final TimeUnit timeUnit) {

		Runnable runner = new Runnable() {
			@Override
			public void run() {
				AutoTimeToLiveCollection.this.removeOldEntries();
			}
		};

		ControllerFactory.createRunOnTimerController().add(this, COLLECTION_LABEL, timeToLive,
				timeToLive, timeUnit, runner);
	}


	/**
	 * Create a collection of objects that are removed from the collection if
	 * they have been in the collection too long.
	 * 
	 * @param timeToLive
	 *            The amount of time each entry has to live.
	 * @param timeUnit
	 *            The unit of time for the time to live value.
	 */
	public AutoTimeToLiveCollection(final long timeToLive, final TimeUnit timeUnit) {

		super(timeToLive, timeUnit);
		init(timeToLive, timeUnit);
	}

}
