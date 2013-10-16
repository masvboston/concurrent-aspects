package com.masvboston.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Utility class for tracking <em>multiple</em> attributes associated with the
 * lifetime of some value. Once the object the attribute belongs to is no longer
 * reachable this tracker will remove all attributes associated with it from
 * storage. This class also provides the ability to specify call back methods in
 * the form of {@link Runnable} instances that it will invoke upon adding a new
 * attribute and prior to releasing the tracking queue for further modification.
 * This class is not thread safe. Calling code must use external synchronization
 * for thread safety.
 * 
 * @author Mark Miller, www.masvboston.com
 * 
 */
public class InstanceAttributeTracker<TInstance, TAttribute, TValue> {
	/**
	 * {@value}
	 */
	private static final String ERROR_MSG_ATTR_NULL = "Attribute cannot be null";

	/**
	 * {@value}
	 */
	private static final String ERROR_MSG_INSTANCE_NULL = "Instance cannot be null";

	/**
	 * {@value}
	 */
	private static final String ERROR_MSG_INSTANCE_ATTR_SAME =
			"Instance and attribute cannot be the same reference";
	/**
	 * Weak hash map of object instances and the methods called on them.
	 */
	private final Map<TInstance, Map<TAttribute, TValue>> objectAttributeCatalog =
			new WeakHashMap<TInstance, Map<TAttribute, TValue>>();


	/**
	 * Check to see if the given attribute on the given object is in the list of
	 * tracked objects.
	 * 
	 * @param instance
	 *            The instance the attribute belongs to. Cannot be null.
	 * @param attribute
	 *            The attribute to track. Cannot be null.
	 * @return True if the attribute of the given instance already recorded,
	 *         false otherwise.
	 */
	public boolean contains(final TInstance instance, final TAttribute attribute) {

		checkBasicInputs(instance, attribute);

		Map<TAttribute, TValue> targetInstance = this.objectAttributeCatalog.get(instance);
		return null == targetInstance ? false : targetInstance.containsKey(attribute);

	}


	/**
	 * Checks to see if the attribute is being tracked and if not it is added to
	 * the tracking system along with any provided value.
	 * 
	 * @param instance
	 *            The instance the method belongs to. Cannot be null.
	 * @param attribute
	 *            The attribute of the instance. Cannot be null.
	 * @param value
	 *            The value of the attribute. This value can be null.
	 * @return True if item added false if it already exists.
	 */
	public boolean checkAndAdd(final TInstance instance, final TAttribute attribute,
			final TValue value) {

		checkBasicInputs(instance, attribute);

		if (!contains(instance, attribute)) {

			add(instance, attribute, value);

			return true;
		}

		return false;
	}


	/**
	 * Utility method to check basic inputs.
	 * 
	 * @param instance
	 *            The object instance cannot be null.
	 * @param attribute
	 *            The attribute cannot be null.
	 */
	protected void checkBasicInputs(final Object instance, final Object attribute) {
		ValidationUtils.checkNull(instance, ERROR_MSG_INSTANCE_NULL);
		ValidationUtils.checkNull(attribute, ERROR_MSG_ATTR_NULL);

		if (instance == attribute) {
			throw new IllegalArgumentException(ERROR_MSG_INSTANCE_ATTR_SAME);
		}
	}


	/**
	 * Method to add entry to catalog that for a given object a attribute is to
	 * be tracked. If the value already exists it will be replaced with the
	 * given values.
	 * 
	 * 
	 * @param instance
	 *            The object the attribute belongs to. Cannot be null.
	 * @param attribute
	 *            The attribute reference. Cannot be null.
	 * @param value
	 *            The value of the attribute to store. This value can be null.
	 * @return The previous value associated with the instance and attribute.
	 *         Null can indicate that either there was no other value at the
	 *         time of addition or null was the value of the attribute.
	 */
	public TValue add(final TInstance instance, final TAttribute attribute, final TValue value) {

		checkBasicInputs(instance, attribute);

		Map<TAttribute, TValue> targetInstance = this.objectAttributeCatalog.get(instance);

		if (null == targetInstance) {

			targetInstance = new HashMap<TAttribute, TValue>();
			this.objectAttributeCatalog.put(instance, targetInstance);
		}

		return targetInstance.put(attribute, value);
	}
}
