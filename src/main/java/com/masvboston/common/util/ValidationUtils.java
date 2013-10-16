package com.masvboston.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Class with methods for various value and range checks. This class provides a
 * number of very useful validation routines that occur often in code. The
 * majority of these methods throw exceptions. These use the naming convention
 * of "check..." preceding the method name.
 * 
 * @author Mark Miller, www.masvboston.com
 * 
 */
public class ValidationUtils {

	/**
	 * {@value}
	 */
	public static final String ERROR_DUPES = "Duplicates found";

	/**
	 * {@value}
	 */
	public static final String ERROR_EXCEPTION_ARG_NULL = "Error argument cannot be null";

	/**
	 * {@value}
	 */
	public static final String ERROR_STR_NULL = "String cannot be null";

	/**
	 * {@value}
	 */
	public static final String ERROR_ARY_EMPTY = "Array is empty";

	/**
	 * {@value}
	 */
	public static final String ERROR_STR_EMPTY = "String is empty";

	/**
	 * {@value}
	 */
	public static final String ERROR_OUT_OF_RANGE =
			"Value of {0} is not in the range of min of {1} and a max of {2}";

	/**
	 * {@value}
	 */
	public static final String ERROR_VALUE_NULL = "Value is null";

	/**
	 * 
	 * @see #checkPropertiesForValues(Object, String, String...)
	 */
	private static final String PROPERTY_ATTR_DELIMITER = "|";

	/**
	 * {@value}
	 */
	private static final String PROPERTY_DELIM_REGEX = "\\|";


	/**
	 * Check the given value to see if it falls within the given min and max,
	 * inclusive, of the given values.
	 * 
	 * @param value
	 *            The value to check.
	 * @param min
	 *            The minimum value, inclusive for the value, pass null for no
	 *            min.
	 * @param max
	 *            The maximum value , inclusive for the value, pass null for no
	 *            max.
	 * @param errorMessage
	 *            The error message to throw if the value is out of the range.
	 *            Null uses the default value: {@value #ERROR_OUT_OF_RANGE}.
	 * @throws IllegalArgumentException
	 *             The value falls outside the given range.
	 */
	public static void checkRange(final double value, Double min, Double max, String errorMessage) {

		min = (null == min) ? Double.MIN_NORMAL : min;
		max = (null == max) ? Double.MAX_VALUE : max;

		errorMessage =
				((null == errorMessage) || (0 == errorMessage.length())) ? ERROR_OUT_OF_RANGE
						: errorMessage;

		if ((value < min) || (value > max)) {
			String msg = MessageFormat.format(errorMessage, value, min, max);
			throw new IllegalArgumentException(msg);
		}
	}


	/**
	 * Check the given value to see if it falls within the given min and max,
	 * inclusive, of the given values.
	 * 
	 * @param value
	 *            The value to check.
	 * @param min
	 *            The minimum value, inclusive for the value, pass null for no
	 *            min.
	 * @param max
	 *            The maximum value , inclusive for the value, pass null for no
	 *            max.
	 * @param errorMessage
	 *            The error message to throw if the value is out of the range.
	 *            Null uses the default value: {@value #ERROR_OUT_OF_RANGE}.
	 * @throws IllegalArgumentException
	 *             The value falls outside the given range.
	 */
	public static void checkRange(final long value, Long min, Long max, final String errorMessage) {

		min = (null == min) ? Long.MIN_VALUE : min;
		max = (null == max) ? Long.MAX_VALUE : max;

		checkRange(Double.valueOf(value), Double.valueOf(min), Double.valueOf(max), errorMessage);

	}


	/**
	 * Check the given value to see if it falls within the given min and max,
	 * inclusive, of the given values.
	 * 
	 * @param value
	 *            The value to check.
	 * @param min
	 *            The minimum value, inclusive for the value, pass null for no
	 *            min.
	 * @param max
	 *            The maximum value , inclusive for the value, pass null for no
	 *            max.
	 * @param errorMessage
	 *            The error message to throw if the value is out of the range.
	 *            Null uses the default value: {@value #ERROR_OUT_OF_RANGE}.
	 * @throws IllegalArgumentException
	 *             The value falls outside the given range.
	 */
	public static void checkRange(final int value, Integer min, Integer max,
			final String errorMessage) {

		min = (null == min) ? Integer.MIN_VALUE : min;
		max = (null == max) ? Integer.MAX_VALUE : max;

		checkRange(Double.valueOf(value), Double.valueOf(min), Double.valueOf(max), errorMessage);

	}


	/**
	 * Checks the given object and throws and exception if it is null.
	 * 
	 * @param value
	 *            the value to check.
	 * @param errorMessage
	 *            Optional message to use if null.
	 */
	public static void checkNull(final Object value, final String... errorMessage) {

		String msg = errorMessage.length > 0 ? errorMessage[0] : ERROR_VALUE_NULL;

		if (null == value) {
			throw msg == null ? new IllegalArgumentException() : new IllegalArgumentException(msg);
		}
	}


	/**
	 * Method to check for null. This is here just for completeness.
	 * 
	 * @param arg
	 *            Any object.
	 * @return True if null false if not.
	 */
	public static boolean isNull(final Object arg) {

		return null == arg;
	}


	/**
	 * Checks the given array to see of any of it's values are null.
	 * 
	 * @param values
	 *            Array to check.
	 * @param errorMessage
	 *            The error message to use. The index of the null is appended to
	 *            the message.
	 */
	public static void checkNull(final Object[] values, final String... errorMessage) {

		String msg = errorMessage.length > 0 ? errorMessage[0] : ERROR_VALUE_NULL;

		if (null == values) {
			throw msg == null ? new IllegalArgumentException() : new IllegalArgumentException(msg);
		}

		msg = msg + " at index ";

		for (int i = 0, n = values.length; i < n; i++) {
			if (null == values[i]) {
				throw new IllegalArgumentException(msg + i);
			}
		}
	}


	/**
	 * Checks the given collection to see of any of it's values are null.
	 * 
	 * @param values
	 *            collection to check.
	 * @param errorMessage
	 *            The error message to use. The index of the null is appended to
	 *            the message.
	 */
	public static <T> void checkNull(final Collection<T> values, final String... errorMessage) {

		String msg = errorMessage.length > 0 ? errorMessage[0] : ERROR_VALUE_NULL;

		if (null == values) {
			throw msg == null ? new IllegalArgumentException() : new IllegalArgumentException(msg);
		}

		msg = msg + " at index ";

		int i = 0;

		for (T value : values) {
			if (null == value) {
				throw new IllegalArgumentException(msg + i);
			}
			i++;
		}
	}


	/**
	 * Checks the given String to see if it is empty.
	 * 
	 * @param value
	 *            String to check.
	 * @param errorMessage
	 *            Optional error message to show.
	 */
	public static void checkEmpty(final String value, final String... errorMessage) {

		String msg = errorMessage.length > 0 ? errorMessage[0] : ERROR_STR_EMPTY;

		if (0 == value.length()) {
			throw msg == null ? new IllegalArgumentException() : new IllegalArgumentException(msg);
		}
	}


	/**
	 * Tests given value to see if it is empty.
	 * 
	 * @param value
	 *            Value to test.
	 * @return True if empty.
	 */
	public static boolean isEmpty(final String value) {

		return 0 == value.length();
	}


	/**
	 * Tests given value to see if it is empty.
	 * 
	 * @param value
	 *            Value to test.
	 * @return True if empty.
	 */
	public static boolean isEmpty(final Collection<?> value) {

		return 0 == value.size();
	}


	/**
	 * Tests given value to see if it is empty.
	 * 
	 * @param value
	 *            Value to test.
	 * @return True if empty.
	 */
	public static boolean isEmpty(final Object[] value) {

		return 0 == value.length;
	}


	/**
	 * Tests given value to see if it is empty.
	 * 
	 * @param value
	 *            Value to test.
	 * @return True if empty.
	 */
	public static boolean isNullOrEmpty(final String value) {

		return isNull(value) || isEmpty(value);
	}


	/**
	 * Tests given value to see if it is empty.
	 * 
	 * @param value
	 *            Value to test.
	 * @return True if empty.
	 */
	public static boolean isNullOrEmpty(final Collection<?> value) {

		return isNull(value) || isEmpty(value);
	}


	/**
	 * Tests given value to see if it is empty.
	 * 
	 * @param value
	 *            Value to test.
	 * @return True if empty.
	 */
	public static boolean isNullOrEmpty(final Object[] value) {

		return isNull(value) || isEmpty(value);
	}


	/**
	 * Checks the given array to see if it is empty.
	 * 
	 * @param value
	 *            Array to check.
	 * @param errorMessage
	 *            Optional error message to show.
	 */
	public static void checkEmpty(final Object[] value, final String... errorMessage) {

		String msg = errorMessage.length > 0 ? errorMessage[0] : ERROR_ARY_EMPTY;

		if (0 == value.length) {
			throw msg == null ? new IllegalArgumentException() : new IllegalArgumentException(msg);
		}
	}


	/**
	 * Checks the given array to see if it is empty.
	 * 
	 * @param value
	 *            Array to check.
	 * @param errorMessage
	 *            Optional error message to show.
	 */
	public static void checkEmpty(final Collection<?> value, final String... errorMessage) {

		String msg = errorMessage.length > 0 ? errorMessage[0] : ERROR_ARY_EMPTY;

		if (0 == value.size()) {
			throw msg == null ? new IllegalArgumentException() : new IllegalArgumentException(msg);
		}
	}


	/**
	 * Convenience method to check if a String is null or empty. This method
	 * does not trim the value if it is not null prior to checking for empty.
	 * 
	 * @param value
	 *            The String to check.
	 * @param errorMessage
	 *            The error message to throw.
	 */
	public static void checkNullOrEmpty(final String value, final String... errorMessage) {

		checkNullOrEmpty(value, false, errorMessage);
	}


	/**
	 * Convenience method to check if a String is null or empty.
	 * 
	 * @param value
	 *            The String to check.
	 * @param trim
	 *            True if a non-null String should be trimmed prior to checking
	 *            if it is empty.
	 * @param errorMessage
	 *            The error message to throw.
	 */
	public static void checkNullOrEmpty(String value, final boolean trim,
			final String... errorMessage) {

		checkNull(value, errorMessage);

		if (trim) {
			value = value.trim();
		}

		if (0 == errorMessage.length) {
			checkEmpty(value);
		}
		else {
			checkEmpty(value, errorMessage[0]);
		}
	}


	/**
	 * Utility method to check for the existence of property values in a given
	 * Java bean. Also checks to ensure the given bean has the property being
	 * checked.
	 * 
	 * @param inputBean
	 *            The Java bean to check.
	 * @param errorMsg
	 *            The error message to throw in addition to the list of
	 *            properties that are missing values. A <strong>null</strong>
	 *            value will have the method use the default, an empty String
	 *            will cause the default message to <strong>not</strong> be
	 *            used.
	 * @param propertyNames
	 *            1 or more property names to check to ensure they have a value.
	 *            Property names can optionally specify a property description
	 *            using a {@value ValidationUtils#PROPERTY_ATTR_DELIMITER}
	 *            delimiter. If provided, the description is the value that will
	 *            appear in the error message otherwise the property name is
	 *            used. This means you can us the property description for
	 *            property name substitution in the outbound error message. For
	 *            example: aknUserName | attributeUserName
	 * 
	 */
	public static void checkPropertiesForValues(final Object inputBean, String errorMsg,
			final String... propertyNames) {

		// Check all the input parameters.
		checkNull(propertyNames, "Property names cannot be null");
		checkEmpty(propertyNames, "Property names cannot be empty");

		for (String property : propertyNames) {
			checkNullOrEmpty(property, "Property name cannot be null or empty");
		}

		// Setup the default message.
		errorMsg = (null == errorMsg) ? "Bean is missing values for properties | " : errorMsg;

		// Get all the properties and their values and check that the property
		// actually has the value.
		try {
			@SuppressWarnings("unchecked")
			Map<String, String> properties = BeanUtils.describe(inputBean);
			StringBuilder errorMessage = new StringBuilder();

			String value = null;
			String propertyDesc;
			String[] propertyAttributes = null;

			for (String property : propertyNames) {

				if (-1 == property.indexOf(PROPERTY_ATTR_DELIMITER)) {

					propertyDesc = property.trim();
				}
				else {

					propertyAttributes = property.split(PROPERTY_DELIM_REGEX);
					property = propertyAttributes[0].trim();
					propertyDesc = propertyAttributes[1].trim();

					if (0 == property.length()) {
						throw new IllegalArgumentException("Property specified is blank");
					}

					if (0 == propertyDesc.length()) {
						throw new IllegalArgumentException("Property description for " + property
								+ " is blank");
					}
				}

				if (!properties.containsKey(property)) {
					throw new IllegalArgumentException("The bean does not have the property: "
							+ propertyDesc);
				}

				value = properties.get(property);

				if ((null == value) || (0 == value.trim().length())) {
					errorMessage.append(propertyDesc).append("\n");
				}
			}

			if (0 < errorMessage.length()) {
				throw new IllegalArgumentException(errorMsg + errorMessage.toString());
			}
		}
		catch (IllegalArgumentException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

	}


	/**
	 * Given an exception, this method retrieves the original exception that
	 * caused the error in the first place. This is not the same as
	 * Exception.getCause, which only returns the exception that lead to the one
	 * you have in hand.
	 * 
	 * @param aException
	 *            The exception to search.
	 * @return The originating exception.
	 */
	public static Throwable extractOriginalException(final Throwable aException) {

		// This method uses recursion.

		checkNull(aException);

		Throwable cause = aException.getCause();
		return null == cause ? aException : extractOriginalException(cause);
	}


	/**
	 * Utility routine that searches an excepiton chain for the first instance
	 * of the given exception. If the given exception matches the given type
	 * then this method will return the given exception.
	 * 
	 * @param aException
	 *            The excepiton to search through.
	 * @param aExceptionClazz
	 *            The exception class to find.
	 * @return The instance of the exception class or null, if none found.
	 */
	public static <E extends Throwable> Throwable searchForException(final Throwable aException,
			final Class<E> aExceptionClazz) {

		checkNull(aException);
		checkNull(aExceptionClazz);

		if (aException.getClass() == aExceptionClazz) {
			return aException;
		}

		Throwable cause = aException.getCause();

		return null == cause ? null : searchForException(cause, aExceptionClazz);

	}


	/**
	 * Extracts the given errors stack trace as a string.
	 * 
	 * @param aError
	 *            The error to process.
	 * @return A string form of the stack trace.
	 */
	public static String stackTraceAsString(final Throwable aError) {

		checkNull(aError, ERROR_EXCEPTION_ARG_NULL);
		StringWriter sw = new StringWriter();
		PrintWriter w = new PrintWriter(sw);

		aError.printStackTrace(w);
		w.flush();
		w.close();

		return sw.toString();
	}


	/**
	 * Checks given array for duplicates and throws an exception if duplicates
	 * are found. The error message identifies the duplicates. The duplicate
	 * values are appended to the given error message if supplied or the default
	 * error message, otherwise.
	 * 
	 * @param values
	 *            The values to check. Given set cannot be null nor can any of
	 *            the values.
	 * @param aMsg
	 *            An optional error message to use instead of the default of
	 *            {@value #ERROR_DUPES}
	 */
	public static void checkForDuplicates(final Object[] values, final String... errorMessage) {

		checkNull(values);
		checkEmpty(values);

		Collection<Object> vals = Arrays.asList(values);

		checkForDuplicates(vals, errorMessage);

	}


	/**
	 * Checks given collection for duplicates and throws an exception if
	 * duplicates are found. The error message identifies the duplicates. The
	 * duplicate values are appended to the given error message if supplied or
	 * the default error message, otherwise.
	 * 
	 * @param values
	 *            The values to check. Given set cannot be null nor can any of
	 *            the values.
	 * @param aMsg
	 *            An optional error message to use instead of the default of
	 *            {@value #ERROR_DUPES}
	 */
	public static void checkForDuplicates(final Collection<?> values, final String... errorMessage) {

		checkNull(values);
		checkEmpty(values);

		String msg = errorMessage.length > 0 ? errorMessage[0] : ERROR_DUPES;

		Set<Object> set = new HashSet<Object>();
		set.addAll(values);

		if (values.size() > set.size()) {

			values.removeAll(set);

			String items = values.toString();

			throw new IllegalArgumentException(msg + ": " + items);
		}

	}

}
