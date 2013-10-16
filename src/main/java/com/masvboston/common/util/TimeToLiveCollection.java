package com.masvboston.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Collection that will expire entries in the collection that linger beyond a
 * given time to live. The expired objects are removed from the collection and
 * garbage collected. The collection checks for any items to remove with every
 * method call except {@link #clear()}.
 * 
 * This class is thread safe.
 * 
 * @author Mark Miller, www.masvboston.com
 * 
 */
public class TimeToLiveCollection<T> implements Collection<T> {

	/**
	 * {@value}
	 */
	private static final String ERROR_BAD_ITEM = "Collection cannot have null items";

	/**
	 * {@value}
	 */
	private static final String ERROR_BAD_COLLECTION = "Target collection cannt be null";

	/**
	 * {@value}
	 */
	private static final String ERROR_BAD_TIME_UNIT = "Time unit cannot be null";

	/**
	 * {@value}
	 */
	private static final String ERROR_BAD_TTL = "Time to live value cannot be less than 1";

	/**
	 * This class is used to encapsulate objects in queue whose life times are
	 * being managed. It defers equals, hash, and to String methods to the
	 * enclosed object instance to enable search and equality checks based on
	 * the contained object, not this enclosing class.
	 * 
	 */
	private static class CollectionEntry implements Delayed {

		private final Object target;
		private final long timeToLive;


		public CollectionEntry(final Object target, final long timeToLive, final TimeUnit timeUnit) {
			this.target = target;
			long time = timeUnit.toMillis(timeToLive);
			this.timeToLive = System.currentTimeMillis() + time;
		}


		public Object getItem() {
			return this.target;
		}


		@Override
		public int compareTo(final Delayed object) {
			if (this == object) {
				return 0;
			}
			long result =
					this.getDelay(TimeUnit.MILLISECONDS) - object.getDelay(TimeUnit.MILLISECONDS);

			if (0 == result) {
				return 0;
			}

			return 0 > result ? -1 : 1;
		}


		@Override
		public long getDelay(final TimeUnit timeUnit) {
			long timeRemaining = this.timeToLive - System.currentTimeMillis();
			return timeUnit.convert(timeRemaining, TimeUnit.MILLISECONDS);
		}


		/**
		 * @return
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return this.target.hashCode();
		}


		/**
		 * @param obj
		 * @return
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object obj) {
			if (obj instanceof CollectionEntry) {
				CollectionEntry item = (CollectionEntry) obj;
				return this.target.equals(item.getItem());
			}
			else {
				return false;
			}
		}


		/**
		 * @return
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return this.target.toString();
		}

	}

	/**
	 * The time unit specified for managing lifetimes for this collection.
	 */
	private final TimeUnit timeUnit;

	/**
	 * The time to live for each item in the list.
	 */
	private final long timePeriod;

	/**
	 * A delayed queue is use to manage which items are removed from the list.
	 */
	private final DelayQueue<CollectionEntry> itemQueue = new DelayQueue<CollectionEntry>();


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
	public TimeToLiveCollection(final Collection<T> targetCollection, final long timeToLive,
			final TimeUnit timeUnit) {
		this(timeToLive, timeUnit);
		ValidationUtils.checkNull(targetCollection, ERROR_BAD_COLLECTION);

		CollectionEntry entry = null;

		for (T item : targetCollection) {
			if (null != item) {
				entry = new CollectionEntry(item, this.timePeriod, this.timeUnit);
				this.itemQueue.add(entry);
			}
			else {
				throw new IllegalStateException(ERROR_BAD_ITEM);
			}
		}
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
	public TimeToLiveCollection(final long timeToLive, final TimeUnit timeUnit) {
		ValidationUtils.checkNull(timeUnit, ERROR_BAD_TIME_UNIT);

		if (1 > timeToLive) {
			throw new IllegalArgumentException(ERROR_BAD_TTL);
		}

		this.timePeriod = timeToLive;
		this.timeUnit = timeUnit;
	}


	/**
	 * @return the unit of time by which the time period to live is measured.
	 */
	public TimeUnit getTimeUnit() {
		return this.timeUnit;
	}


	/**
	 * @return the time period to live.
	 */
	public long getTimeToLive() {
		return this.timePeriod;
	}


	/**
	 * Clears all expired entries from the queue and returns the number removed.
	 * 
	 * @return The number of entries removed.
	 */
	@SuppressWarnings("unchecked")
	public int removeOldEntries() {
		int result = 0;
		CollectionEntry di = null;

		while (null != (di = this.itemQueue.poll())) {
			result++;
			doOnItemExpiration((T) di.getItem());
		}

		return result;
	}


	@Override
	public boolean add(final T arg) {
		removeOldEntries();

		if (null == arg) {
			return false;
		}

		CollectionEntry item = new CollectionEntry(arg, this.timePeriod, this.timeUnit);
		return this.itemQueue.add(item);
	}


	@Override
	public boolean addAll(final Collection<? extends T> arg) {
		removeOldEntries();

		if ((null == arg) || (0 == arg.size())) {
			return false;
		}

		int adds = 0;

		for (T item : arg) {
			this.add(item);
			adds++;
		}

		return 0 < adds;
	}


	@Override
	public void clear() {
		this.itemQueue.clear();
	}


	@Override
	public boolean contains(final Object arg) {
		removeOldEntries();

		if (null == arg) {
			return false;
		}

		CollectionEntry item = new CollectionEntry(arg, this.timePeriod, this.timeUnit);
		return this.itemQueue.contains(item);
	}


	@Override
	public boolean containsAll(final Collection<?> arg) {
		removeOldEntries();

		if ((null == arg) || (0 == arg.size())) {
			return false;
		}

		for (Object item : arg) {
			if (!this.contains(item)) {
				return false;
			}
		}

		return true;
	}


	@Override
	public boolean isEmpty() {
		removeOldEntries();
		return this.itemQueue.isEmpty();
	}


	@Override
	public Iterator<T> iterator() {
		removeOldEntries();
		final Iterator<CollectionEntry> internalIt = this.itemQueue.iterator();

		final Iterator<T> itResult = new Iterator<T>() {

			@Override
			public boolean hasNext() {
				return internalIt.hasNext();
			}


			@SuppressWarnings("unchecked")
			@Override
			public T next() {
				return (T) internalIt.next().getItem();
			}


			@Override
			public void remove() {
				internalIt.remove();
			}
		};

		return itResult;
	}


	@Override
	public boolean remove(Object arg) {
		removeOldEntries();

		if (null == arg) {
			return false;
		}

		arg = new CollectionEntry(arg, this.timePeriod, this.timeUnit);
		return this.itemQueue.remove(arg);
	}


	@Override
	public boolean removeAll(final Collection<?> arg) {
		removeOldEntries();

		if ((null == arg) || (0 == arg.size())) {
			return false;
		}

		int count = 0;

		for (Object item : arg) {
			this.remove(item);
			count++;
		}

		return 0 < count;
	}


	@Override
	public boolean retainAll(final Collection<?> arg) {
		removeOldEntries();

		if ((null == arg) || (0 == arg.size())) {
			return false;
		}

		Iterator<T> items = this.iterator();

		T item = null;

		int count = 0;

		while (items.hasNext()) {
			item = items.next();

			if (!arg.contains(item)) {
				items.remove();
				count++;
			}
		}

		return 0 < count;
	}


	@Override
	public int size() {
		removeOldEntries();
		return this.itemQueue.size();
	}


	@Override
	public Object[] toArray() {
		removeOldEntries();
		ArrayList<Object> list = new ArrayList<Object>(this.itemQueue.size());

		for (CollectionEntry entry : this.itemQueue) {
			list.add(entry.getItem());
		}

		return list.toArray();
	}


	@Override
	public <P> P[] toArray(final P[] arg) {
		removeOldEntries();
		ArrayList<Object> list = new ArrayList<Object>(this.itemQueue.size());

		for (CollectionEntry entry : this.itemQueue) {
			list.add(entry.getItem());
		}

		return list.toArray(arg);
	}


	/**
	 * Event method, override to intercept item expiration events.
	 * 
	 * @param item
	 *            The item that expired.
	 */
	protected void doOnItemExpiration(final T item) {
		// Subclasses can override to intercept item expiration events.
	}
}
